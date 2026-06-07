package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.horario_tienda
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularTiempoRestante
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.calcularTiempoRestante_box
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.capitalizeFirst
@Composable
fun retornar_color_estado_tienda_Box(
    id_tienda: String,
    horario_total: HorarioDia_box,
    tick: Long,
    pagado: Boolean,
    color: (Color, String) -> Unit,
    mostrar_txt: Boolean = true,
) {
    Log.d("hoaraiossssss12312313131313123", horario_total.toString())

    val resultado by remember(horario_total, tick) {
        derivedStateOf { calcularTiempoRestante_box(horario_total) }
    }

    if (pagado && mostrar_txt) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(resultado.color)
            )
            spacer_horizonta(5.dp)
            TextoExpandibleEnLinea(
                resultado.texto.capitalizeFirst(),
                resultado.color,
                resultado.color
            )
        }
    } else if (!pagado) {
        Text(
            text = "Consultar al negocio",
            color = Color(0xFFA5A5A5),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    color(resultado.color, resultado.texto)
}


@Composable
fun TiempoRestanteCierre(
    horario_total: horario_tienda,
    hCierre: String,
    cerrado: Boolean,
    motivo: String,
    pagado: Boolean,
    max_line: Int = 1,
    tick: Long,
    color: (Color) -> Unit
) {

    Log.d("horario_total", horario_total.toString())
    val resultado by remember(horario_total, hCierre, cerrado, motivo, tick) {
        derivedStateOf { calcularTiempoRestante(horario_total, hCierre, cerrado, motivo) }
    }
    if (pagado) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(resultado.color)
            )
            spacer_horizonta(5.dp)
            TextoExpandibleEnLinea(
                resultado.texto.capitalizeFirst(),
                resultado.color,
                resultado.color
            )
            color(resultado.color)
        }
    } else {
        Text(
            text = "Consultar al negocio",
            color = Color(0xFFA5A5A5),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun retornar_color_estado_tienda(
    horario_total: horario_tienda,
    hCierre: String,
    cerrado: Boolean,
    motivo: String,
    tick: Long,
    color: (Color) -> Unit
) {
    val resultado by remember(horario_total, hCierre, cerrado, motivo, tick) {
        derivedStateOf { calcularTiempoRestante(horario_total, hCierre, cerrado, motivo) }
    }
    color(resultado.color)
}

@Composable
fun texto_tiempo_restante(
    horario_total: horario_tienda,
    hCierre: String,
    cerrado: Boolean,
    motivo: String,
    tick: Long,
    txt: (String) -> Unit
) {
    Log.d("horario_total", horario_total.toString())
    val resultado by remember(horario_total, hCierre, cerrado, motivo, tick) {
        derivedStateOf { calcularTiempoRestante(horario_total, hCierre, cerrado, motivo) }
    }
    txt(resultado.texto.capitalizeFirst())

}