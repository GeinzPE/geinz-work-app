package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.dataclass_review.data_class_review
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_pleaceholder_label
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.viewModels.viewmodel_review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_review(data_class_review: data_class_review, ondimis: () -> Unit) {
    val viewmodel: viewmodel_review= viewModel()
    val _datos_TL_review=viewmodel._datos_TL_review.observeAsState()
    LaunchedEffect(data_class_review) {
        viewmodel.set_datos_TL_review(data_class_review)
    }
    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.background
    ) {
        var ratingValue by remember { mutableStateOf(1) }
        Column(modifier = Modifier.padding(10.dp)) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(_datos_TL_review.value?.imagen ?: R.drawable.cargando_img_categorias)

                        .size (200,200)
                        .placeholder(R.drawable.cargando_img_categorias)
                        .error(R.drawable.cargando_img_categorias)
                        .build(),
                    contentDescription = "Imagen de la tienda",
                    contentScale = ContentScale.Crop,

                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp)

                        .shadow(
                            elevation = 24.dp, // más altura = sombra más visible
                            ambientColor = Color.White.copy(alpha = 0.8f), // más brillante
                            spotColor = Color.White.copy(alpha = 0.6f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                )



            }
            FullStarRating(
                starSize = 40.dp,
                onRatingChanged = { newRating ->
                    ratingValue = newRating
                    println("Calificación actual: $ratingValue")
                }
            )

            OutlinedTextField(
                value = "",
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(20.dp),
                label = { retornar_pleaceholder_label("Déjanos tu opinión") },
                placeholder = { retornar_pleaceholder_label("Déjanos tu opinión") },
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = false,
                maxLines = 10,   // número máximo de líneas que puede crecer
                minLines = 4     // tamaño inicial
            )

            Button(onClick = {}) { texto_generico_one_line("Calificar") }

        }
    }
}

@Composable
fun FullStarRating(
    modifier: Modifier = Modifier,
    starSize: Dp = 50.dp,
    maxStars: Int = 5,
    onRatingChanged: (Int) -> Unit
) {
    var rating by remember { mutableStateOf(0) }

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