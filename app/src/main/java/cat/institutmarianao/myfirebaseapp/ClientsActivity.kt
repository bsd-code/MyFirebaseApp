package cat.institutmarianao.myfirebaseapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

private const val TAG = "Firestore"

class ClientsActivity : AppCompatActivity() {

    // 1. Inicializamos Firestore
    private val db = Firebase.firestore

    private lateinit var emailEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var adapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clients)

        // Las reglas de Firestore exigen usuario autenticado:
        // si no hay sesión, volvemos a la pantalla de login
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            finish()
            return
        }

        val currentUserTextView = findViewById<TextView>(R.id.currentUserTextView)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        currentUserTextView.text = currentUser.email

        logoutButton.setOnClickListener {
            AuthUI.getInstance().signOut(this).addOnCompleteListener {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        emailEditText = findViewById(R.id.emailEditText)
        nameEditText = findViewById(R.id.nameEditText)
        ageEditText = findViewById(R.id.ageEditText)
        val saveButton = findViewById<Button>(R.id.saveClientButton)
        val clearButton = findViewById<Button>(R.id.clearButton)
        val recyclerView = findViewById<RecyclerView>(R.id.clientsRecyclerView)

        // RecyclerView + adaptador
        adapter = ClientAdapter(
            clients = emptyList(),
            onClientClick = { client -> mostrarEnFormulario(client) },
            onDeleteClick = { client -> eliminarCliente(client) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        saveButton.setOnClickListener { guardarCliente() }
        clearButton.setOnClickListener { limpiarFormulario() }

        // Leemos la colección al abrir la pantalla
        cargarClientes()
    }

    // ==============================================
    // CREAR / ACTUALIZAR
    // El email es el documentId: si no existe se crea,
    // si ya existe se sobrescriben todos sus campos.
    // ==============================================
    private fun guardarCliente() {
        val email = emailEditText.text.toString().trim()
        val name = nameEditText.text.toString().trim()
        val age = ageEditText.text.toString().trim().toIntOrNull()

        if (email.isEmpty() || name.isEmpty() || age == null) {
            mensaje("Rellena email, nombre y una edad válida")
            return
        }

        val client = Client(email = email, name = name, age = age)

        db.collection("clients")
            .document(email)
            .set(client)
            .addOnSuccessListener {
                Log.d(TAG, "Document saved with ID: $email")
                mensaje("Cliente guardado")
                limpiarFormulario()
                cargarClientes()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error saving document", e)
                mensaje("Error: ${e.message}")
            }
    }

    // ==============================================
    // LEER toda la colección
    // ==============================================
    private fun cargarClientes() {
        db.collection("clients")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                adapter.updateClients(result.toObjects(Client::class.java))
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting documents", e)
                mensaje("Error: ${e.message}")
            }
    }

    // ==============================================
    // ELIMINAR
    // ==============================================
    private fun eliminarCliente(client: Client) {
        db.collection("clients")
            .document(client.email) // Fem servir l'email com a ID del document
            .delete()
            .addOnSuccessListener {
                mensaje("Cliente eliminado") // 1. Muestra el mensaje emergente (Toast)
                limpiarFormulario()
                cargarClientes() // 2. Vuelve a consultar la base de datos para actualizar el RecyclerView
            }
            .addOnFailureListener { e ->
                mensaje("Error: ${e.message}") // Muestra un Toast con la descripción del error que ha ocurrido
            }
    }

    // ==============================================
    // Ayudas del formulario
    // ==============================================
    private fun mostrarEnFormulario(client: Client) {
        emailEditText.setText(client.email)
        nameEditText.setText(client.name)
        ageEditText.setText(client.age.toString())
        emailEditText.isEnabled = false // Deshabilitamos la edición del email
    }

    private fun limpiarFormulario() {
        emailEditText.setText("")
        nameEditText.setText("")
        ageEditText.setText("")
        emailEditText.isEnabled = true // Volvemos a habilitarlo para crear un cliente nuevo
    }

    private fun mensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }
}