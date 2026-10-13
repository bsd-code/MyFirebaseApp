package cat.institutmarianao.myfirebaseapp

// Modelo de un cliente. Firestore necesita valores por defecto
// (constructor sin argumentos) para poder hacer toObject().
data class Client(
    val email: String = "",
    val name: String = "",
    val age: Int = 0
)