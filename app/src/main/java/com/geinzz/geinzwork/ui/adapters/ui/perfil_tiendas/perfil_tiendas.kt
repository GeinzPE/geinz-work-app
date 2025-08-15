//package com.geinzz.geinzwork.ui.adapters.ui.perfil_tiendas
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.InlineTextContent
//import androidx.compose.foundation.text.appendInlineContent
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.Placeholder
//import androidx.compose.ui.text.PlaceholderVerticalAlign
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil3.compose.AsyncImage
//import coil3.compose.rememberAsyncImagePainter
//import com.geinzz.geinzwork.R
//import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.tags_subcateogiras
//import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.text_expandible_wrapp
//import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
//import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
//import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
//import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.ZoomIconButton
//
//@Composable
//fun UI_principal_principal_tiendas(expandido: () -> Unit, mostrarDialogozoom: () -> Unit) {
//    Box() {
//        AsyncImage(
//            model = img_tienda_perfil,
//            contentDescription = "Imagen de la tienda",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(220.dp)
//                .clip(RoundedCornerShape(16.dp))
//                .clickable { expandido() },
//            onState = { state ->
//            } 
//        )
//        Box(
//            modifier = Modifier.align(Alignment.BottomEnd)
//        ) {
//            ZoomIconButton(mostrarDialogozoom)
//        }
//    }
//}
//
//@Composable
//fun perfil_cabezero() {
//    Column {
//        val iconId = "icon"
//        val annotatedText = buildAnnotatedString {
//            append(nombre_tienda.uppercase())
//            append(" ")
//            appendInlineContent(iconId, "[icon]")
//        }
//
//        val inlineContent = mapOf(
//            iconId to InlineTextContent(
//                Placeholder(
//                    width = 20.sp,
//                    height = 20.sp,
//                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
//                )
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(15.dp)
//                        .clip(RoundedCornerShape(50))
//                        .background(estadoColor)
//                )
//            }
//        )
//
//        Text(
//            text = annotatedText,
//            inlineContent = inlineContent,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 10.dp),
//            style = MaterialTheme.typography.titleLarge,
//            color = MaterialTheme.colorScheme.onBackground,
//            maxLines = 2,
//            overflow = TextOverflow.Ellipsis,
//        )
//
//        text_expandible_wrapp(
//            "Categoria : $categoritienda",
//            MaterialTheme.typography.bodyMedium
//        )
//        spacer_vertical(10.dp)
//
//        tags_subcateogiras(lista_tags)
//    }
//}
//
//@Composable
//fun abrir_google_maps() {
//    FloatingActionButton(
//        onClick = {
//            constantes_lista_localidades.abrir_google_maps(
//                context,
//                latitud,
//                longitud
//            ) { dialogo ->
//                mostrarDialogo(dialogo)
//            }
//        },
//        modifier = Modifier.size(40.dp),
//        containerColor = MaterialTheme.colorScheme.primary,
//    ) {
//        Image(
//            painter = painterResource(R.drawable.localidad_icon_general),
//            contentDescription = "Localidad",
//            modifier = Modifier.size(35.dp)
//        )
//    }
//}
//
//
//@Composable
//fun descripcion_tienda(texto_descipcion_tiendas: String) {
//    texto_generico_multilinea(texto_descipcion_tiendas)
//}
//
//@Composable
//fun direccion_referencias(){
//
//}