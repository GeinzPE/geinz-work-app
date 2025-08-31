package com.geinzz.geinzwork.ui.adapters.ui.ui.theme

import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.R

val baners_geinz_work = FontFamily(
    Font(R.font.baners_geinz_work),
)
val textos_titulos_geinz_wokr = FontFamily(
    Font(R.font.textos_titulos_normales_geinz_work),
)
val busqueda_principal_geinz_work = FontFamily(
    Font(R.font.titulo_geinz_work),
)


val Typography.textosTituloGeinzWork: TextStyle
    get() = TextStyle(
        fontFamily = textos_titulos_geinz_wokr,
        fontWeight = FontWeight.Bold,

        fontSize = 20.sp
    )

val Typography.banerGeinzWork: TextStyle
    get() = TextStyle(
        fontFamily = baners_geinz_work,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
    )

val Typography.busquedaGeinzWork: TextStyle
    get() = TextStyle(
        fontFamily = busqueda_principal_geinz_work,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
    )

// Set of Material typography styles to start with
val Typography = Typography(

    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
    ),


    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        fontFamily = FontFamily.Default,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        fontFamily = FontFamily.Default,
    ),

    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 19.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 13.sp,
    )


    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)