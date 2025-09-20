package com.geinzz.geinzwork.ui.adapters.ui.pantallas.salud_seguridad

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_seguridad.dataclass_seguridad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.permisos_llamadas
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.dialog_sin_ubicacion_activa
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import java.net.URLEncoder

private val REQUEST_CALL_PHONE = 1

@Composable
fun ui_salud_seguirdad(
    viewmode_segurirdad_Salud: viewmode_seguridad_salud,
    localida: String,
    abrir_mapa: (latitud: Double, longitud: Double) -> Unit
) {
    val lista_seguridad_salud by viewmode_segurirdad_Salud._datos_lugares.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewmode_segurirdad_Salud.obtener_servicios(localida)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn {
            items(lista_seguridad_salud) { i ->
                Box(modifier = Modifier.padding(8.dp)) {
                    carta_salud_cuidad(i, abrir_mapa = { la, lo ->
                        viewmode_segurirdad_Salud.setCoordenadas(la, lo)
                        abrir_mapa(la, lo)
                    })
                }
            }
        }

    }
}

@Composable
fun carta_salud_cuidad(
    i: dataclass_seguridad,
    abrir_mapa: (latitud: Double, longitud: Double) -> Unit
) {
    val context = LocalContext.current
    var dialogo_activar_ubicacion by remember { mutableStateOf(false) }

    var call_dialog_permise by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.surface)
            .padding(5.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(i.img_ref)
                .size(300, 100).placeholder(R.drawable.cargando_img_categorias)
                .error(R.drawable.cargando_img_categorias)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(5)),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.padding(5.dp)) {
            texto_generico_one_line(
                i.nombre_,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            spacer_vertical(5.dp)
            texto_generico_one_line("direccion : ${i.direccion}", color = Color.White)
            spacer_vertical(5.dp)
            texto_generico_one_line("Abierto", color = Color.White)
            spacer_vertical(10.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                BtnCirculares(R.drawable.llamada_icon, fondo = MaterialTheme.colorScheme.primary) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CALL_PHONE
                        ) != PackageManager.PERMISSION_GRANTED
                    ){
                        call_dialog_permise=true
                    }else{
                        makePhoneCall(context, i.numero_whatsapp)
                    }
                }
                BtnCirculares(R.drawable.whatsapp_icon) {
                    val msje = "hola"
                    val uri = Uri.parse(
                        "https://api.whatsapp.com/send?phone=${i.numero_whatsapp}&text=${
                            URLEncoder.encode(
                                msje,
                                "UTF-8"
                            )
                        }"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "no se pudo abrir whatsapp", Toast.LENGTH_LONG)
                            .show()
                    }

                }
                BtnCirculares(
                    R.drawable.vector_ruta_icon,
                    fondo = MaterialTheme.colorScheme.primary
                ) {
                    constantes_lista_localidades.abrir_google_maps(
                        context = context,
                        i.latidud, i.longitud
                    ) { mostrar_dialog ->
                        dialogo_activar_ubicacion = mostrar_dialog
                    }
                }
                BtnCirculares(Icons.Default.Map, fondo = MaterialTheme.colorScheme.secondary) {
                    abrir_mapa(i.latidud, i.longitud)
                }
            }
        }
    }
    if(call_dialog_permise){
        permisos_llamadas(aceptar_permisos={
            requestCallPermission(context, i.numero_llamada)
        },ondimis={
            call_dialog_permise=false
        })
    }
    if (dialogo_activar_ubicacion) {
        dialog_sin_ubicacion_activa(
            onDismis = {
                dialogo_activar_ubicacion = false
            },
            abrir_configuracion = {
                dialogo_activar_ubicacion = false
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
            dialog_sin_maps = {
                dialogo_activar_ubicacion = false
            }
        )
    }
}

@Composable
fun BtnCirculares(
    icono: Any,
    fondo: Color = Color.Transparent,
    size: Dp = 32.dp,
    iconSize: Dp = 22.dp,
    tint: Color = Color.White,
    listener: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(fondo)
            .clickable { listener() },
        contentAlignment = Alignment.Center
    ) {
        when (icono) {
            is Int -> Image(
                painter = painterResource(id = icono),
                contentDescription = null,
            )

            is ImageVector -> Icon(
                imageVector = icono,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = tint
            )
        }
    }
}

private fun requestCallPermission(context: Context, phoneNumber: String) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.CALL_PHONE),
            REQUEST_CALL_PHONE
        )
    } else {
        makePhoneCall(context, phoneNumber)
    }
}
private fun makePhoneCall(context: Context, phoneNumber: String) {
    val callIntent = Intent(Intent.ACTION_CALL)
    callIntent.data = Uri.parse("tel:$phoneNumber")
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        context.startActivity(callIntent)
    } else {
        requestCallPermission(context, phoneNumber)
    }
}