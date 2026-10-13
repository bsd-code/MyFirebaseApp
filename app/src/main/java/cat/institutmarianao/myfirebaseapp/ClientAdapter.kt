package cat.institutmarianao.myfirebaseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClientAdapter(
    private var clients: List<Client>,
    private val onClientClick: (Client) -> Unit,
    private val onDeleteClick: (Client) -> Unit
) : RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

    // Guarda las vistas de cada fila (item_client.xml)
    class ClientViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val emailTextView: TextView = view.findViewById(R.id.emailTextView)
        val ageTextView: TextView = view.findViewById(R.id.ageTextView)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val client = clients[position]

        holder.nameTextView.text = client.name
        holder.emailTextView.text = client.email
        holder.ageTextView.text = client.age.toString()

        // Click en la fila: carregar el cliente en el formulario para editarlo
        holder.itemView.setOnClickListener { onClientClick(client) }

        // Click en Delete: borrar el cliente
        holder.deleteButton.setOnClickListener { onDeleteClick(client) }
    }

    override fun getItemCount(): Int = clients.size

    fun updateClients(newClients: List<Client>) {
        clients = newClients
        notifyDataSetChanged()
    }
}