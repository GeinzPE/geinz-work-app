import androidx.compose.ui.unit.Dp
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val nombre: String,
    val lugar: String,
    val id_tienda: String,
    val categoria: String,
    val img: String,
    val lista: List<String>,


)

