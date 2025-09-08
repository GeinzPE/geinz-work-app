import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val nombre: String,
    val lugar: String,
    val categoria: String,
    val img: String,
    val lista: List<String>
)

