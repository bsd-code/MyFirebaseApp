package cat.institutmarianao.myfirebaseapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Conexión con Firebase Authentication
    private val auth = FirebaseAuth.getInstance()

    private lateinit var userTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var goToClientsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userTextView = findViewById(R.id.userTextView)
        logoutButton = findViewById(R.id.logoutButton)
        goToClientsButton = findViewById(R.id.goToClientsButton)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val dialogButton = findViewById<Button>(R.id.accesPerDialegButton)

        // LOGIN normal
        loginButton.setOnClickListener {
            login(
                emailEditText.text.toString().trim(),
                passwordEditText.text.toString().trim()
            )
        }

        // REGISTER (con diálogo)
        registerButton.setOnClickListener {
            mostrarDialogo("Registrarse", "Registrar") { email, password ->
                registrar(email, password)
            }
        }

        // LOGIN por diálogo
        dialogButton.setOnClickListener {
            mostrarDialogo("Login", "Login") { email, password ->
                login(email, password)
            }
        }

        // LOGOUT
        logoutButton.setOnClickListener {
            auth.signOut()
            actualizarUsuario()
            mensaje("Sesión cerrada")
        }

        // Ir a la pantalla de clientes (solo visible con sesión iniciada)
        goToClientsButton.setOnClickListener {
            startActivity(Intent(this, ClientsActivity::class.java))
        }

        val accessFirebaseUiButton = findViewById<Button>(R.id.accessFirebaseUiButton)

        accessFirebaseUiButton.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                // User already signed in, launch ClientsActivity
                startActivity(Intent(this, ClientsActivity::class.java))
            } else {
                // Not signed in, show firebase login dialog
                launchFirebaseAuthUI()
            }
        }
    }

    // Cada vez que se muestra la pantalla, refrescamos quién está logueado
    override fun onStart() {
        super.onStart()
        actualizarUsuario()
    }

    // ==============================================
    // LOGIN
    // ==============================================
    private fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            mensaje("Email and password required")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mensaje("Login correcto")
                    actualizarUsuario()
                    startActivity(Intent(this, ClientsActivity::class.java))
                } else {
                    mensaje("Error: ${task.exception?.message}", largo = true)
                }
            }
    }

    // ==============================================
    // REGISTRO
    // (Firebase ya deja al usuario logueado tras crear la cuenta)
    // ==============================================
    private fun registrar(email: String, password: String) {
        // Si están vacíos, Firebase lanza una excepción y la app se cierra
        if (email.isEmpty() || password.isEmpty()) {
            mensaje("Email and password required")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mensaje("Usuario creado!")
                    actualizarUsuario()
                } else {
                    // Por ejemplo: contraseña de menos de 6 caracteres
                    mensaje("Error: ${task.exception?.message}", largo = true)
                }
            }
    }

    // ==============================================
    // DIÁLOGO reutilizable (sirve para login y registro)
    // ==============================================
    private fun mostrarDialogo(
        titulo: String,
        textoBoton: String,
        accion: (String, String) -> Unit
    ) {
        val vista = LayoutInflater.from(this)
            .inflate(R.layout.activity_dialog_login, null)

        val email = vista.findViewById<EditText>(R.id.emailEditText)
        val password = vista.findViewById<EditText>(R.id.passwordEditText)

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(vista)
            .setPositiveButton(textoBoton) { _, _ ->
                accion(
                    email.text.toString().trim(),
                    password.text.toString().trim()
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ==============================================
    // Muestra el usuario actual y los botones que dependen de la sesión
    // ==============================================
    private fun actualizarUsuario() {
        val user = auth.currentUser

        if (user != null) {
            userTextView.text = getString(R.string.main_user_logged, user.email)
            logoutButton.visibility = View.VISIBLE
            goToClientsButton.visibility = View.VISIBLE
        } else {
            userTextView.text = getString(R.string.main_user_not_logged)
            logoutButton.visibility = View.GONE
            goToClientsButton.visibility = View.GONE
        }
    }

    private fun mensaje(texto: String, largo: Boolean = false) {
        Toast.makeText(
            this,
            texto,
            if (largo) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Login successful
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ClientsActivity::class.java))
            } else {
                // Login failed or cancelled
                Toast.makeText(this, "Login cancelled or failed", Toast.LENGTH_SHORT).show()
            }
        }

    private fun launchFirebaseAuthUI() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder()
                .setAllowNewAccounts(false) // Avoid FirebaseUI to register
                .build()
        )

        val signInIntent =
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build()

        signInLauncher.launch(signInIntent)
    }
}