package com.geinzz.geinzwork.ui.adapters.ui.ui.theme

import android.app.Activity
import android.graphics.drawable.shapes.Shape
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(

    //primario color de botones ,chips,floatins
    //background = fondo

    background = fondo_oscuro5_s,
    onBackground = color_texto_oscuro_background,
    primary = btn_floatin40,
    onPrimary = texto_oscuro100,

    surface = color_carta_oscuro,
    surfaceVariant = color_carta_oscuro35,

    primaryContainer = color_chips_select,

    secondary = btn_floatin40,

    tertiary = Pink80
)

//private val LightColorScheme = lightColorScheme(
//    background = fondo_calro95,
//
//    onBackground = color_texto_claro_background,
//
//    primary = btn_floatin40,
//    onPrimary = texto_claro0,
//
//    surface = color_carta_claro90,
//    surfaceVariant = color_carta_claro35,
//
//
//    primaryContainer = color_chips_select,
//
//    secondary = btn_floatin40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

//@Composable
//fun GeinzWorkTheme(
//    darkTheme: Boolean =true,
////    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = false,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context)
////            else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
////        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content,
//        shapes = Shapes(
//            small = RoundedCornerShape(10.dp),
//            medium = RoundedCornerShape(15.dp)
//        )
//    )
//}

@Composable
fun GeinzWorkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(15.dp)
        ),
        content = content
    )
}
