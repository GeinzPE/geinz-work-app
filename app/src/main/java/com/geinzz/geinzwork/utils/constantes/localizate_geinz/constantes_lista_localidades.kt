
package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_Dia
import com.geinzz.geinzwork.data.model.localizate_geinz.onboarding.dataclass_onboarding
import com.geinzz.geinzwork.data.model.localizate_geinz.onboarding.dataclass_pantalla1
import com.geinzz.geinzwork.utils.localizate_geinz.abrirRutaEnGoogleMaps
import com.geinzz.geinzwork.utils.localizate_geinz.verificarUbiActiva
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object constantes_lista_localidades {
    val lista = listOf(
        dataclass_localidad_escudos("Barranca".lowercase(), R.drawable.escudo_barranca),
        dataclass_localidad_escudos("Paramonga".lowercase(), R.drawable.escudo_paramonga),
        dataclass_localidad_escudos("Supe".lowercase(), R.drawable.escudo_supe),
        dataclass_localidad_escudos("Pativilca".lowercase(), R.drawable.escudo_pativilca)
    )
    val dias_sema =
        listOf(
            "lunes", "martes", "miércoles",
            "jueves", "viernes", "sábado", "domingo"
        )

    val lista_localidad = listOf("Barranca", "Supe", "paramonga", "pativilca", "Puerto supe")

    fun obtenerMetodoContacto(
        metodo: String,
        data: Map<String, Any>
    ): Pair<Boolean, String> {
        val metodoData = data[metodo] as? Map<String, Any> ?: emptyMap()
        val estado = metodoData["estado"] as? Boolean ?: false
        val nombre =
            metodoData["nombre_buscador"] as? String ?: metodoData["numero"] as? String ?: ""
        return estado to nombre
    }

    fun ocultarNumero(numero: String): String {
        return if (numero.length >= 9) {
            val primerosTres = numero.take(3)
            val ocultos = "*".repeat(numero.length - 3)
            "$primerosTres$ocultos"
        } else {
            numero
        }
    }
    fun getCategoriaIcon(categoria: String): String {
        return when (categoria.lowercase()) {
            "bancos y servicios financieros" -> "🏦"
            "belleza" -> "💅"
            "comida y restaurantes" -> "🍽️"
            "deporte y bienestar" -> "🏋️"
            "educacion y librerias" -> "📚"
            "entretenimiento y recreacion" -> "🎭"
            "grifos y estaciones" -> "⛽"
            "hogar y ferreteria" -> "🛠️"
            "hospedaje y entretenimiento nocturno" -> "🏨"
            "jardineria y plantas" -> "🌱"
            "lavanderias y tintorerias" -> "👕"
            "mascotas y animales" -> "🐾"
            "mecanica y autoservicios" -> "🔧"
            "minimarkets y bodegas" -> "🛒"
            "moda y estilo" -> "👗"
            "salud y farmacias" -> "💊"
            "servicios de encomienda y envios" -> "📦"
            "servicios tecnicos y reparaciones" -> "🔌"
            "supermercados y tiendas grandes" -> "🏬"
            "tecnologia y electronica" -> "💻"
            "transporte y terminales" -> "🚌"
            else -> "🏷️" // genérico
        }
    }


    fun verificarSiEstaAbierto(lista_horarios_por_tienda: List<horario_Dia>): Boolean {
        return try {
            val diaActualConTilde =
                SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).lowercase()
            val diaActual = diaActualConTilde
            Log.d("HORARIO_CHECK", "Día actual: $diaActual")

            val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
            val ahora = formato.parse(formato.format(Date())) ?: return false
            Log.d("HORARIO_CHECK", "Hora actual: ${formato.format(ahora)}")

            lista_horarios_por_tienda.forEach { i ->
                val dia = i.dia!!.lowercase()
                Log.d("HORARIO_CHECK", "Evaluando día: $dia")
                if (dia == diaActual) {
                    val apertura = formato.parse(i.h_apertura)
                    val cierre = formato.parse(i.h_cierre)
                    Log.d(
                        "HORARIO_CHECK",
                        "Horario -> Apertura: ${i.h_apertura}, Cierre: ${i.h_cierre}"
                    )

                    if (apertura == null || cierre == null) {
                        Log.w("HORARIO_CHECK", "Horario inválido, se omite este día.")
                        return@forEach
                    }

                    val estaAbierto = if (cierre.after(apertura)) {
                        // Ejemplo: 08:00 - 18:00
                        ahora in apertura..cierre
                    } else {
                        // Ejemplo: 22:00 - 02:00 (día siguiente)
                        ahora.after(apertura) || ahora.before(cierre)
                    }

                    Log.d("HORARIO_CHECK", "¿Está abierto hoy? $estaAbierto")

                    if (estaAbierto) return true
                }
            }

            false // Ningún horario coincide y está activo
        } catch (e: Exception) {
            Log.e("verificarSiEstaAbierto", "Error al verificar horario", e)
            false
        }
    }

    fun quitarTildes(texto: String): String {
        val normalized = Normalizer.normalize(texto, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    fun abrir_google_maps(
        context: Context,
        latitud: Double,
        longitud: Double,
        mostrar_dialog: (Boolean) -> Unit
    ) {
        if (verificarUbiActiva(context)) {
            abrirRutaEnGoogleMaps(context, latitud, longitud)
        } else {
            mostrar_dialog(true)
        }
    }

    @Composable
    fun ZoomIconButton(mostrarDialogozoom: () -> Unit) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { mostrarDialogozoom() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.verctor_zoom_white),
                contentDescription = "Ocultar",
                modifier = Modifier.size(18.dp)
            )
        }
    }


    fun cambiar_icono_exapndible(expandido: Boolean): Int {

        return if (expandido) {
            R.drawable.ocultar_abajo
        } else {
            R.drawable.ocultar_arriva
        }
    }

    val lista_img_local = listOf(
        R.drawable.f1,
        R.drawable.f2,
        R.drawable.f4,
        R.drawable.f5,
        R.drawable.f6,
        R.drawable.f7,
        R.drawable.f8
    )


    val lista_img_localidades_local = listOf(
        R.drawable.f2,
        R.drawable.f4,
        R.drawable.f5,
        R.drawable.f7,
        R.drawable.f8,
    )
    val lista_img_localidades_nombre = listOf(
        dataclass_onboarding(R.drawable.f2, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f4, "Puerto ", "Playa de puerto supe"),
        dataclass_onboarding(R.drawable.f5, "pativilca ", "Paza de armas de pativilca"),
        dataclass_onboarding(R.drawable.f7, "Supe ", "Casa de las brujas supe"),
        dataclass_onboarding(R.drawable.f8, "Paramonga ", "Plaza de armas de paramonga"),
    )

    val lista_img_carga = listOf(
        dataclass_onboarding(R.drawable.f1, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f2, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f3, "Barranca ", "Plaza de armas barranca"),
        dataclass_onboarding(R.drawable.f4, "Puerto ", "Playa de puerto supe"),
        dataclass_onboarding(R.drawable.f5, "pativilca ", "Paza de armas de pativilca"),
        dataclass_onboarding(R.drawable.f7, "Supe ", "Casa de las brujas supe"),
        dataclass_onboarding(R.drawable.f8, "Paramonga ", "Plaza de armas de paramonga"),
    )

    val lista_frases_busqueda = listOf(
        "¿Qué buscas?",
        "Descubre algo",
        "Explora tu ciudad",
        "¿Qué descubrir?",
        "Busca tiendas",
        "Explora promos",
        "Todo en un lugar",
        "Descubre cerca",
        "¿Qué explorar?"
    )


    val chips_filtrado_busqueda=listOf(
        "Todos","comida y restaurantes","Hoteles","Lugares"
    )


    val lista_color_degradado_bottom = listOf(
        Color.Black.copy(alpha = 1f),   // arriba: oscuro
        Color.Black.copy(alpha = 0.85f), // muy oscuro
        Color.Black.copy(alpha = 0.6f),  // intermedio
        Color.Black.copy(alpha = 0.3f),// casi transparente
        Color.Transparent
    )
    val lista_color_degradado_top = listOf(
        Color.Transparent,
        // inicio
        Color.Black.copy(alpha = 0.2f),        // un poco oscuro
        Color.Black.copy(alpha = 0.5f),        // intermedio
        Color.Black.copy(alpha = 0.85f),       // ya casi negro
        Color.Black.copy(alpha = 0.95f),       // más negro aún
        Color.Black.copy(alpha = 1f)
    )

    val frasesCarga = listOf(
        "Bienvenido, tu espacio te espera.",
        "Relájate, estamos preparando todo para ti.",
        "Cada lugar tiene su historia, la tuya empieza aquí.",
        "Explora y disfruta de lo que tu ciudad ofrece.",
        "Tu tiempo es valioso, lo cuidamos por ti.",
        "Descubre algo nuevo en cada esquina.",
        "Pequeños momentos, grandes experiencias.",
        "Cerca de ti, todo lo que necesitas.",
        "Bienvenido de vuelta, te hemos extrañado.",
        "Tu próxima aventura comienza aquí.",
        "Calles, luces y lugares para descubrir.",
        "Tu ciudad tiene secretos, vamos a encontrarlos.",
        "Cada paso cuenta, explora sin prisa.",
        "Lo que buscas, lo encuentras cerca.",
        "Entre tiendas y locales, todo tiene su encanto.",
        "Sabores, colores y momentos por descubrir.",
        "Aquí, tu rutina se convierte en experiencia.",
        "Cada esquina guarda algo especial para ti.",
        "Siente la ciudad, vive cada momento.",
        "La aventura urbana empieza en tu pantalla."
    )


    val lista_frances_inicio_seccion = listOf(
        "Encuentra tu lugar favorito",
        "Descubre nuevos destinos",
        "Encuentra lo que necesitas hoy",
        "Vive momentos inolvidables",
        "Tu próxima aventura comienza aquí"
    )

    val lista_fraces_inicio = listOf(
        "Explora sin límites",
        "Tu próxima parada",
        "Lo mejor está aquí",
        "Encuentra tu lugar"
    )

    val fracespantalla1 = listOf(
        dataclass_pantalla1(
            "Tu camino más fácil",
            "Encuentra rápido las tiendas y servicios que necesitas cerca de ti. Todo en un solo lugar, para que tu día sea más simple.",
            R.drawable.p1_1
        ),
        dataclass_pantalla1(
            "Explora tu zona",
            "Descubre restaurantes, tiendas y servicios en tu ciudad. Aprovecha promociones exclusivas y conoce lo que tienes alrededor.",
            R.drawable.p1_2
        ),
        dataclass_pantalla1(
            "Rutas rápidas",
            "Sigue rutas directas y seguras para llegar más rápido a tu destino. Encuentra siempre lo que buscas sin complicaciones.",
            R.drawable.p1_3
        )
    )


    fun saludo_user_principal(nombre: String): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val (saludo, emojiExtra) = when (hora) {
            in 6..11 -> "Hola, buenos días" to "🌞"
            in 12..18 -> "Hola, buenas tardes" to "\uD83C\uDF24\uFE0F"
            else -> "Hola, buenas noches" to "🌙"
        }

        return "$saludo, $nombre $emojiExtra"
    }


    fun esGmailValido(correo: String): Boolean {
        val regex = Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$")
        return regex.matches(correo)
    }

    fun convertirHora24a12(hora24: String): String {
        val formato24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formato12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = formato24.parse(hora24)
        return formato12.format(date!!)
    }




//    val listaCategorias = listOf(
//        dataclass_cat_sub(
//            "belleza", listOf(
//                "peluquerias",
//                "barberias",
//                "spas",
//                "salones de unas",
//                "centros esteticos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "comida y restaurantes", listOf(
//                "pollerias",
//                "chifas",
//                "pizzerias",
//                "cevicherias",
//                "restaurantes criollos",
//                "comida rapida",
//                "pastelerias",
//                "cafeterias",
//                "heladerias"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "educacion y librerias", listOf(
//                "institutos educativos",
//                "universidades privadas",
//                "colegios privados",
//                "librerias"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "hogar y ferreteria", listOf(
//                "mueblerias",
//                "vidrierias",
//                "ferreterias",
//                "tiendas de decoracion",
//                "tiendas de electrodomesticos",
//                "colchoneras",
//                "tiendas de iluminacion"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "moda y estilo", listOf(
//                "tiendas de ropa",
//                "tiendas de calzado",
//                "ropa deportiva",
//                "accesorios de moda",
//                "boutiques"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "salud y farmacias", listOf(
//                "boticas",
//                "farmacias",
//                "consultorios medicos",
//                "laboratorios clinicos",
//                "opticas",
//                "consultorios dentales"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "mascotas y animales", listOf(
//                "veterinarias",
//                "tiendas para mascotas",
//                "alimentos para mascotas",
//                "accesorios para mascotas",
//                "banos y peluqueria canina"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "supermercados y tiendas grandes", listOf(
//                "supermercados",
//                "mayoristas",
//                "tiendas por departamento",
//                "mercados centrales"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "minimarkets y bodegas", listOf(
//                "minimarkets",
//                "bodegas",
//                "licorerias",
//                "abarrotes",
//                "distribuidoras de agua y gas"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "transporte y terminales", listOf(
//                "paraderos de moto",
//                "paraderos de combi",
//                "terminales terrestres",
//                "agencias de transporte",
//                "cooperativas de transporte"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "viajes y turismo", listOf(
//                "agencias de viaje",
//                "paquetes turisticos",
//                "turismo local",
//                "boletos terrestres y aereos"
//            ), emptyList()
//        ),
//
//        dataclass_cat_sub(
//            "jardineria y plantas", listOf(
//                "viveros",
//                "tiendas de plantas",
//                "tiendas de abonos y fertilizantes",
//                "control de plagas",
//                "productos para jardin"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "grifos y estaciones", listOf(
//                "grifos",
//                "estaciones de servicio",
//                "venta de gasolina",
//                "venta de gas vehicular",
//                "lubricentros"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "bancos y servicios financieros", listOf(
//                "bancos",
//                "cajas municipales",
//                "cooperativas",
//                "casas de cambio",
//                "agencias financieras"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "servicios tecnicos y reparaciones", listOf(
//                "reparacion de celulares",
//                "reparacion de laptops y computadoras",
//                "reparacion de televisores",
//                "reparacion de refrigeradoras",
//                "reparacion de licuadoras",
//                "reparacion de electrodomesticos",
//                "servicios de lavado de electrodomesticos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "mecanica y autoservicios", listOf(
//                "mecanica de motos",
//                "mecanica de autos",
//                "repuestos para motos",
//                "repuestos para autos",
//                "talleres de motos",
//                "talleres de autos",
//                "lavado de vehiculos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "entretenimiento y recreacion", listOf(
//                "billares",
//                "casas de apuestas",
//                "salas de videojuegos",
//                "cabinas de internet"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "tecnologia y electronica", listOf(
//                "venta de celulares",
//                "venta de accesorios para celulares",
//                "venta de computadoras",
//                "venta de partes y perifericos",
//                "tiendas de electronica menor"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "fotografia e impresion", listOf(
//                "cabinas fotograficas",
//                "fotografias para dni y carnet",
//                "servicios de copias",
//                "plastificados y escaneos",
//                "imprentas"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "deporte y bienestar", listOf(
//                "gimnasios",
//                "centros de yoga",
//                "centros fitness",
//                "centros de pilates"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "utiles y papelerias", listOf(
//                "tiendas de utiles escolares",
//                "papelerias",
//                "venta de materiales de oficina",
//                "venta de cuadernos y lapiceros"
//            ), emptyList()
//        ),
//
//        dataclass_cat_sub(
//            "lavanderias y tintorerias", listOf(
//                "lavanderias",
//                "tintorerias",
//                "lavado en seco",
//                "planchado de ropa"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "servicios de encomienda y envios", listOf(
//                "agencias de encomienda",
//                "courier local",
//                "envios nacionales",
//                "servicios de delivery",
//                "paqueteria"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "funerarias", listOf(
//                "servicios funerarios",
//                "velatorios",
//                "venta de ataudes",
//                "traslados funerarios",
//                "cremacion"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "hospedaje y entretenimiento nocturno", listOf(
//                "hoteles",
//                "hostales",
//                "hospedajes",
//                "discotecas",
//                "bares",
//                "karaokes",
//                "salones de eventos",
//                "moteles"
//            ), emptyList()
//        )
//    )
}