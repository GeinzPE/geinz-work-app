package com.geinzz.geinzwork.ui.adapters.ui.dialog_general


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_map
import com.geinzz.geinzwork.model.repo_eres_socio
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_cerra_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import com.google.firebase.firestore.model.mutation.ArrayTransformOperation

@Composable
fun dialog_eliminar_favoritos(
    viewModelFiltros: viewModel_filtado_tiendas,localidad_tienda:String,
    id_user:String,id_tienda:String,
    nombre_tienda: String,
    ondimis: () -> Unit,
    aceptado:()-> Unit
) {
val repo_eres_socio= repo_eres_socio()
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general (Color.Red){
                ondimis()
                viewModelFiltros.eliminar_tienda_favorita(id_user,id_tienda,localidad_tienda)
                repo_eres_socio.restar_contador("guardados",localidad_tienda,id_tienda)
                aceptado()
            }
        },
        dismissButton = {
            btn_cerra_etc_dialog_general {
                ondimis()
            }
        },
        text = {
            FuenteControladaApp {
                Column() {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(R.drawable.icon_3d_corazon_roto),
                            contentDescription = "",
                            modifier = Modifier.size(45.dp)
                        )
                    }
                    spacer_vertical(10.dp)
                    texto_generico_multilinea(
                        "¿Eliminar de Favoritos?",
                        MaterialTheme.typography.titleLarge
                    )
                    spacer_vertical(10.dp)
                    texto_generico_multilinea(
                        "¿Deseas quitar $nombre_tienda de tu lista? Podrás agregarlo de nuevo cuando quieras.",
                        MaterialTheme.typography.bodyMedium
                    )

                }
            }
        }
    )
}