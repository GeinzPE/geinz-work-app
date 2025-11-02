package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.login.crear_cuenta_geinz
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.baners_geinz_work
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.FuenteControladaApp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottom_sheet_registrate(
    ondimis: () -> Unit,
    iniciar_seccion_normal: () -> Unit,
    crear_cuenta_geinz: () -> Unit,
    texto_bottom_Sheet: String
) {


    ModalBottomSheet(
        onDismissRequest = { ondimis() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        FuenteControladaApp {
            Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF8700F3).copy(alpha = 0.7f),
                                        Color.Transparent
                                    ),
                                ),
                                shape = RoundedCornerShape(200.dp)
                            )
                    )
                    Image(
                        painter = painterResource(R.drawable.logo_geinz_blanco),
                        contentDescription = "logo",
                        modifier = Modifier.size(60.dp)
                    )
                }

                Text(
                    text = texto_bottom_Sheet,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontFamily = baners_geinz_work,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                spacer_vertical(20.dp)
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ), modifier = Modifier.fillMaxWidth(), onClick = { iniciar_seccion_normal() }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        texto_generico_one_line(
                            "Ir a iniciar sesión",
                            MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 17.dp)
                        )
                        spacer_horizonta(10.dp)
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Flecha continuar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                }
                spacer_vertical(15.dp)
                crear_cuenta_geinz { crear_cuenta_geinz() }
                spacer_vertical(15.dp)
            }
        }
    }

}