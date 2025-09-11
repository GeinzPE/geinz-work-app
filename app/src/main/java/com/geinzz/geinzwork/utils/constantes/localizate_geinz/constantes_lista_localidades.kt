package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.data.model.localizate_geinz.horario_Dia
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.ref_ubi
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.seguridad_salud_publica
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


    val chips_filtrado_busqueda = listOf(
        "Todos", "comida y restaurantes", "Hoteles", "Lugares"
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
        "¿Listo para empezar?",
        "¿Qué planes tienes ?",
        "¿Exploramos juntos?",
        "¿A dónde quieres ir?",
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
//    @OptIn(ExperimentalMaterial3Api::class)
//    @Composable
//    fun CarouselExample_MultiBrowse() {
//        data class CarouselItem(
//            val id: Int,
//            @DrawableRes val imageResId: Int,
//            val contentDescription: String
//        )
//
//        val items = remember {
////            listOf(
////                CarouselItem(0, R.drawable.cupcake, "cupcake"),
////                CarouselItem(1, R.drawable.donut, "donut"),
////                CarouselItem(2, R.drawable.eclair, "eclair"),
////                CarouselItem(3, R.drawable.froyo, "froyo"),
////                CarouselItem(4, R.drawable.gingerbread, "gingerbread"),
////            )
//        }
//
//        HorizontalMultiBrowseCarousel(
//            state = rememberCarouselState { items.count() },
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentHeight()
//                .padding(top = 16.dp, bottom = 16.dp),
//            preferredItemWidth = 186.dp,
//            itemSpacing = 8.dp,
//            contentPadding = PaddingValues(horizontal = 16.dp)
//        ) { i ->
//            val item = items[i]
//            Image(
//                modifier = Modifier
//                    .height(205.dp)
//                    .maskClip(MaterialTheme.shapes.extraLarge),
//                painter = painterResource(id = item.imageResId),
//                contentDescription = item.contentDescription,
//                contentScale = ContentScale.Crop
//            )
//        }
//    }

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

    val lista_img_seguridad = listOf(
        R.drawable.barranca_comisaria,
        R.drawable.bomberos_brca,
        R.drawable.samu_brca,
        R.drawable.bomberos_brca,R.drawable.supe_brca,
        R.drawable.hospital_brca
    )


//    val lista_constantes = listOf(
//        // Barranca
//        seguridad_salud_publica(
//            nombre = "Hospital de Barranca",
//            tipo = "salud",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.753746104394681,
//                longitud = -77.76360404577927,
//                referencia = "Frente a la Plaza de Armas",
//                direccion = "Av. Nicolás de Piérola 210–224"
//            ),
//            numero_contacto = listOf("012352241", "012352075", "012352156")
//        ),
//
//        seguridad_salud_publica(
//            nombre = "DIPINCRI Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.741253339348441,
//                longitud = -77.76520851349262,
//                referencia = "Frente a la Municipalidad",
//                direccion = "Jr. Grau s/n, Barranca"
//            ),
//            numero_contacto = listOf("012352350") // ejemplo de contacto oficial, ajustable
//        ),
//
//        seguridad_salud_publica(
//            nombre = "Puestos de Salud Barranca",
//            tipo = "salud",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = 0.0,
//                longitud = 0.0,
//                referencia = "Sectores: Buenavista, Chiu Chiu, Purmacana, Potao",
//                direccion = "Varios sectores"
//            ),
//            numero_contacto = listOf("012352075")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.750510578838727,
//                longitud = -77.76504890345076,
//                referencia = "",
//                direccion = "Calle Independencia s/n"
//            ),
//            numero_contacto = listOf("012354905", "012354906")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría de Carreteras Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.753936505455542,
//                longitud = -77.75880483155137,
//                referencia = "",
//                direccion = "Calle José Gálvez 490"
//            ),
//            numero_contacto = listOf("012352302")
//        ),
//        seguridad_salud_publica(
//            nombre = "Serenazgo Municipal Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.741717151744634,
//                longitud = -77.76324690256669,
//                referencia = "",
//                direccion = ""
//            ),
//            numero_contacto = listOf("900872784")
//        ),
//        seguridad_salud_publica(
//            nombre = "SAMU Barranca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.75215146325845,
//                longitud = -77.76316724577927,
//                referencia = "",
//                direccion = ""
//            ),
//            numero_contacto = listOf("948162002")
//        ),
//        seguridad_salud_publica(
//            nombre = "Compañía de Bomberos Voluntarios Barranca N° 73",
//            tipo = "seguridad",
//            img = "",
//            localidad = "barranca",
//            datos_ubi = ref_ubi(
//                latitud = -10.754105881347417,
//                longitud = -77.76056763413719,
//                referencia = "",
//                direccion = "Plaza de Armas s/n"
//            ),
//            numero_contacto = listOf("012352333")
//        ),
//
//        // Paramonga
//        seguridad_salud_publica(
//            nombre = "Centro de Salud Paramonga José Luis Flores Mallqui",
//            tipo = "salud",
//            img = "",
//            localidad = "paramonga",
//            datos_ubi = ref_ubi(
//                latitud = -10.679865987621321,
//                longitud = -77.8163874169446,
//                referencia = "Frente a la Plaza de Armas",
//                direccion = "Urb. 7 de Junio, Calle Francisco Vidal s/n"
//            ),
//            numero_contacto = listOf("012360738")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Paramonga",
//            tipo = "seguridad",
//            img = "",
//            localidad = "paramonga",
//            datos_ubi = ref_ubi(
//                latitud = -10.673341757309588,
//                longitud = -77.81932444948157,
//                referencia = "",
//                direccion = "Av. Central N° 275"
//            ),
//            numero_contacto = listOf("2360082", "2362854")
//        ),
//
//        seguridad_salud_publica(
//            nombre = "Compañía de Bomberos Voluntarios Salvadora Paramonga N° 81",
//            tipo = "seguridad",
//            img = "",
//            localidad = "paramonga",
//            datos_ubi = ref_ubi(
//                latitud = -10.673108494600752,
//                longitud = -77.82089603101512,
//                referencia = "",
//                direccion = "Av. Central 131"
//            ),
//            numero_contacto = listOf("012360329")
//        ),
//
//        // Supe
//        seguridad_salud_publica(
//            nombre = "Hospital de Supe Laura Esther Rodríguez Dulanto",
//            tipo = "salud",
//            img = "",
//            localidad = "supe",
//            datos_ubi = ref_ubi(
//                latitud = -10.796884035820383,
//                longitud = -77.71720251256687,
//                referencia = "",
//                direccion = "Jr. Alfonso Ugarte 350"
//            ),
//            numero_contacto = listOf("930954779", "991335459")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Supe Pueblo",
//            tipo = "seguridad",
//            img = "",
//            localidad = "supe",
//            datos_ubi = ref_ubi(
//                latitud = -10.796013479381218,
//                longitud = -77.71522440097667,
//                referencia = "",
//                direccion = "Jr. Sucre 350"
//            ),
//            numero_contacto = listOf("2364304")
//        ),
//
//
//        // Pativilca
//        seguridad_salud_publica(
//            nombre = "Centro de Salud Pativilca",
//            tipo = "salud",
//            img = "",
//            localidad = "pativilca",
//            datos_ubi = ref_ubi(
//                latitud = -10.696178344630095,
//                longitud = -77.77991647824568,
//                referencia = "",
//                direccion = "Jr. Simón Bolívar 125"
//            ),
//            numero_contacto = listOf("2363406")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Pativilca",
//            tipo = "seguridad",
//            img = "",
//            localidad = "pativilca",
//            datos_ubi = ref_ubi(
//                latitud = -10.69593001890776,
//                longitud = -77.77828829745266,
//                referencia = "",
//                direccion = "Jr. Simón Bolívar 117"
//            ),
//            numero_contacto = listOf("2363406")
//        ),
//        seguridad_salud_publica(
//            nombre = "Bomberos Pativilca N° 91",
//            tipo = "seguridad",
//            img = "",
//            localidad = "pativilca",
//            datos_ubi = ref_ubi(
//                latitud = -10.693173894803182,
//                longitud = -77.78186228182595,
//                referencia = "",
//                direccion = "Av. San Martin 295"
//            ),
//            numero_contacto = listOf("012360329")
//        ),
//
//
//        // Supe Puerto
//        seguridad_salud_publica(
//            nombre = "Puesto de salud nueva victoria",
//            tipo = "salud",
//            img = "",
//            localidad = "supe_puerto",
//            datos_ubi = ref_ubi(
//                latitud = -10.798229224689468,
//                longitud = -77.7395314515867,
//                referencia = "",
//                direccion = "C. San Pedro 201, Supe Puerto 15162"
//            ),
//            numero_contacto = listOf("2364008")
//        ),
//        seguridad_salud_publica(
//            nombre = "Comisaría PNP Supe Puerto",
//            tipo = "seguridad",
//            img = "",
//            localidad = "supe_puerto",
//            datos_ubi = ref_ubi(
//                latitud = -10.797019308906867,
//                longitud = -77.74180885176115,
//                referencia = "",
//                direccion = "Jr. Callao 501"
//            ),
//            numero_contacto = listOf("2364008")
//        ),
//
//        )


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