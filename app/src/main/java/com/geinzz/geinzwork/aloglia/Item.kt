import androidx.compose.ui.unit.Dp
import com.geinzz.geinzwork.model.CategoryWithSubcategories
import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val nombre: String="",
    val lugar: String="",
    val id_tienda: String="",
    val categoria: String="",
    val img: String="",
    val lista: List<String> = emptyList(),
    val latitud: Double=0.0,
    val longitud: Double=0.0
)

@Serializable
data class Resultado_sub_cat(
    val categoria: String,
//    val subcategoria: String? = null,
//    val listaItems: List<Item> = emptyList()
)

