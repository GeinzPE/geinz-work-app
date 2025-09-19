package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.data.model.dataclass_review.datos_review
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.principal_ui.crearReview
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.estaDentroDeTienda
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import com.geinzz.geinzwork.viewModels.viewmodel_review
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth


private lateinit var firebaseAuth: FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_review(
    tipo: String,
    viewmodel: viewmodel_review,
    data_class_review: data_class_review,
    ondimis: () -> Unit,
    clik_envio: (Int, String) -> Unit,
) {
    firebaseAuth = FirebaseAuth.getInstance()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val _datos_TL_review = viewmodel._datos_TL_review.observeAsState()
    val _verificar_review_exsit = viewmodel._verificar_review_exsit.observeAsState()
    val _review_send = viewmodel._review_send.observeAsState(initial = false)
    var texto by remember { mutableStateOf("") }
    var ratingValue by remember { mutableStateOf(0) }

    // cargar datos de la tienda
    LaunchedEffect(data_class_review) {
        viewmodel.set_datos_TL_review(data_class_review)
    }

    // cerrar bottomsheet al enviar review
    LaunchedEffect(_review_send.value) {
        if (_review_send.value) {
            ondimis()
            viewmodel.resetar_valor_review()
        }
    }

    // verificar si ya existe review cuando hay usuario
    LaunchedEffect(firebaseAuth.currentUser) {
        firebaseAuth.currentUser?.let {
            viewmodel.verificar_review_existente(it.uid, data_class_review)
        }
    }

    // cargar datos existentes en los estados
    LaunchedEffect(_verificar_review_exsit.value) {
        _verificar_review_exsit.value?.let { (puntaje, descrip) ->
            Log.d("reviewss", "${puntaje} ${descrip}")
            texto = descrip
            ratingValue = puntaje
        }
    }

    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        sheetState = sheetState,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        if (firebaseAuth.currentUser != null) {
            texto_generico_one_line("abiertotttt bottom sheet")
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(
                                _datos_TL_review.value?.imagen
                                    ?: R.drawable.cargando_img_categorias
                            )
                            .size(200, 200)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        contentDescription = "Imagen de la tienda",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(200.dp)
                            .height(200.dp)
                            .shadow(
                                elevation = 24.dp,
                                ambientColor = Color.White.copy(alpha = 0.8f),
                                spotColor = Color.White.copy(alpha = 0.6f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                    )
                    spacer_vertical(10.dp)
                    texto_generico_one_line(_datos_TL_review.value?.nombre.toString())

                }



                FullStarRating(
                    starSize = 40.dp,
                    onRatingChanged = { newRating ->
                        ratingValue = newRating
                    },
                    initialRating = ratingValue,
                )

                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(20.dp),
                    label = { retornar_pleaceholder_label("Déjanos tu opinión") },
                    placeholder = { retornar_pleaceholder_label("Déjanos tu opinión") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = false,
                    maxLines = 10,
                    minLines = 4
                )

                when (tipo) {
                    "normal" -> {
                        Button(onClick = {
                            clik_envio(ratingValue, texto)
                        }) {
                            texto_generico_one_line("Calificar")
                        }
                    }
                }

            }
        } else {
            texto_generico_one_line("Necesitas registrarte para dejar una reseña")
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_Sheet_seguro(
    viewmodel: viewmodel_review,
    data_class_review: data_class_review,
    ondimis: () -> Unit, clik_envio: (Int, String, Location?) -> Unit,
) {

    firebaseAuth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var ubicacionPrevia by remember { mutableStateOf<Location?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val _datos_TL_review = viewmodel._datos_TL_review.observeAsState()
    val _verificar_review_exsit = viewmodel._verificar_review_exsit.observeAsState()
    val _review_send = viewmodel._review_send.observeAsState(initial = false)
    var texto by remember { mutableStateOf("") }
    var ratingValue by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            ubicacionPrevia = location
            Log.d("ReviewUbicacion", "Ubicación prefetch -> $location")
        }
    }
    // cargar datos de la tienda
    LaunchedEffect(data_class_review) {
        viewmodel.set_datos_TL_review(data_class_review)
    }

    // cerrar bottomsheet al enviar review
    LaunchedEffect(_review_send.value) {
        if (_review_send.value) {
            ondimis()
            viewmodel.resetar_valor_review()
        }
    }

    // verificar si ya existe review cuando hay usuario
    LaunchedEffect(firebaseAuth.currentUser) {
        firebaseAuth.currentUser?.let {
            viewmodel.verificar_review_existente(it.uid, data_class_review)
        }
    }

    // cargar datos existentes en los estados
    LaunchedEffect(_verificar_review_exsit.value) {
        _verificar_review_exsit.value?.let { (puntaje, descrip) ->
            Log.d("reviewss", "${puntaje} ${descrip}")
            texto = descrip
            ratingValue = puntaje
        }
    }

    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        sheetState = sheetState,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        if (firebaseAuth.currentUser != null) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {

                texto_generico_one_line("Cerrado bottom sheet")
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(
                                _datos_TL_review.value?.imagen ?: R.drawable.cargando_img_categorias
                            )
                            .size(200, 200)
                            .placeholder(R.drawable.cargando_img_categorias)
                            .error(R.drawable.cargando_img_categorias)
                            .build(),
                        contentDescription = "Imagen de la tienda",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(200.dp)
                            .height(200.dp)
                            .shadow(
                                elevation = 24.dp,
                                ambientColor = Color.White.copy(alpha = 0.8f),
                                spotColor = Color.White.copy(alpha = 0.6f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                FullStarRating(
                    starSize = 40.dp,
                    onRatingChanged = { newRating ->
                        ratingValue = newRating
                    },
                    initialRating = ratingValue,
                )

                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(20.dp),
                    label = { retornar_pleaceholder_label("Déjanos tu opinión") },
                    placeholder = { retornar_pleaceholder_label("Déjanos tu opinión") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = false,
                    maxLines = 10,
                    minLines = 4
                )





                Button(onClick = {
                    clik_envio(ratingValue, texto, ubicacionPrevia)
                }) {
                    texto_generico_one_line("Calificar")
                }


            }
        } else {
            texto_generico_one_line("Necesitas registrarte para dejar una reseña")
        }
    }
}

@Composable
fun FullStarRating(
    modifier: Modifier = Modifier,
    starSize: Dp = 50.dp,
    maxStars: Int = 5,
    initialRating: Int = 0,
    onRatingChanged: (Int) -> Unit
) {

    var rating by remember { mutableStateOf(initialRating) }

    LaunchedEffect(initialRating) {
        rating = initialRating
    }

    Row(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val x = change.position.x
                    val starWidth = size.width / maxStars
                    val newRating = ((x / starWidth).toInt() + 1).coerceIn(0, maxStars)
                    rating = newRating
                    onRatingChanged(rating)
                }
            }
            .height(starSize)
    ) {
        for (i in 1..maxStars) {
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Star $i",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

