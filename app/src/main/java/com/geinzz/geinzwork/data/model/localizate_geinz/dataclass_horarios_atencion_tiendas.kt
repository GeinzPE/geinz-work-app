package com.geinzz.geinzwork.data.model.localizate_geinz

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


//data class encontradas_por_categoria(
//    val cantidad_registradas: Int?,
//    val activas: Int?,
//    val categoria: String?,
//    val subcateogiras: List<String>,
//    val img_subcategorias: String=""
//)

data class encontradas_por_categoria(
    val categoria: String?,
    val subcateogiras: List<String>,
    val img_subcategorias: String=""
)


data class horario_Dia(
    val dia: String="",
    val h_apertura: String="",
    val h_cierre: String=""
)

data class HorarioTienda(
    val id_tienda: String="",
    val lista_Horario: List<horario_Dia> = emptyList()
)

data class tiendas_patrocinadas(
    val id_tienda: String="",
    val categoria_tienda: String=""
)



data class modelo_tienda(
    val nombre_tienda: String="",
    val modelo_negocio: Boolean=false,
    val localidad: String?="",
    val categoria_tienda: String="",
    val descripcion: String="",
    val id_tienda: String="",
    val img_perfil: String="",
    val lista_img: List<String> =emptyList(),
    val subcategoria: List<String> = emptyList(),
    val ubicacion: Map<String, Any> = emptyMap(),
    val pagado: Boolean=false,
    val metodo_contacto_tienda:metodo_contacto_tienda= metodo_contacto_tienda(),
    val horario_atencion: HorarioAtencion = HorarioAtencion(), // 🔹 Aquí se agrega el horario
    val metodos_pago_tienda:modelo_pagos_tienda =modelo_pagos_tienda(),
    val horario_tienda_box:HorarioAtencion_box=HorarioAtencion_box()
)
@Parcelize
data class modelo_pagos_tienda(
    val visa_mastercard: modelo_metodo_individual = modelo_metodo_individual(),
    val agora: modelo_metodo_individual = modelo_metodo_individual(),
    val efectivo: modelo_metodo_individual = modelo_metodo_individual(),
    val plin: modelo_metodo_individual = modelo_metodo_individual(),
    val yape: modelo_metodo_individual = modelo_metodo_individual()
): Parcelable

@Parcelize
data class modelo_metodo_individual(
    val numero: String="",
    val qr:String="",
    val nombre:String="",
    val enable: Boolean=false
): Parcelable

@Parcelize
data class metodo_contacto_tienda(
    val whatsapp: contacto_numero = contacto_numero(),
    val llamada: contacto_numero = contacto_numero(),
    val facebook: contacto_red = contacto_red(),
    val instagram: contacto_red = contacto_red(),
    val tiktok: contacto_red = contacto_red(),
    val sitio_web: contacto_red = contacto_red()
) : Parcelable

@Parcelize
data class contacto_numero(
    val estado: Boolean = false,
    val numero: String = ""
) : Parcelable

@Parcelize
data class contacto_red(
    val estado: Boolean = false,
    val nombre: String = "",
    val url: String = ""
) : Parcelable

data class HorarioDia(
    val cerrado: Boolean = false,
    val h_apertura: String = "",
    val h_cierre: String = "",
    val motivo: String = ""
)
@Parcelize
data class HorarioBloque(
    val h_apertura: String,      // "09:00"
    val h_cierre: String,        // "14:00"
): Parcelable

@Parcelize
data class HorarioDia_bloques(
    val bloques: List<HorarioBloque> = emptyList(),
    val cerrado: Boolean = false, // si el día completo está cerrado
    val motivo: String = ""       // motivo del cierre del día
): Parcelable

data class HorarioAtencion(
    val lunes: HorarioDia = HorarioDia(),
    val martes: HorarioDia = HorarioDia(),
    val miercoles: HorarioDia = HorarioDia(),
    val jueves: HorarioDia = HorarioDia(),
    val viernes: HorarioDia = HorarioDia(),
    val sabado: HorarioDia = HorarioDia(),
    val domingo: HorarioDia = HorarioDia()
)
@Parcelize
data class HorarioAtencion_box(
    val lunes: HorarioDia_bloques = HorarioDia_bloques(),
    val martes: HorarioDia_bloques = HorarioDia_bloques(),
    val miércoles: HorarioDia_bloques = HorarioDia_bloques(),
    val jueves: HorarioDia_bloques = HorarioDia_bloques(),
    val viernes: HorarioDia_bloques = HorarioDia_bloques(),
    val sábado: HorarioDia_bloques = HorarioDia_bloques(),
    val domingo: HorarioDia_bloques = HorarioDia_bloques()
): Parcelable


//
//data class modelo_tienda_temporal(
//    val nombre_tienda: String="",
//    val modelo_negocio: Boolean=false,
//    val localidad: String?="",
//    val categoria_tienda: String="",
//    val descripcion: String="",
//    val id_tienda: String="",
//    val img_perfil: String="",
//    val lista_img: List<String> =emptyList(),
//    val subcategoria: List<String> = emptyList(),
//    val ubicacion: Map<String, Any> = emptyMap(),
//    val metodo_contacto: Map<String, Any> = emptyMap(),
//
//    val whatsapp: Boolean = false,
//    val numero_whatsapp: String = "",
//    val tiktok: Boolean = false,
//    val nombre_tiktok: String = "",
//    val sitio_web: Boolean = false,
//    val url_sitio_web: String = "",
//    val instagram: Boolean = false,
//    val nombre_user_ig: String = "",
//    val facebook: Boolean = false,
//    val nombre_user_fb: String = ""
//
//)




//val lista_agregar_tiendas_brca = listOf(
////barranca
//    modelo_tienda_temporal(
//        nombre_tienda = "Delicias del Norte Barranca",
//        modelo_negocio = true,
//        localidad = "barranca",
//        categoria_tienda = "comida y restaurantes",
//        descripcion = "Delicias del Norte en barranca, especializado en pastelerias",
//        id_tienda = "78e64d21-7946-43f7-9",
//        img_perfil = "",
//        subcategoria = listOf("pastelerias"),
//        ubicacion = mapOf(
//            "latitud" to -10.719152,
//            "longitud" to -77.759149,
//            "dirección" to "Calle 5 Mz D Lt 1",
//            "referencia" to "Frente a parque San Juan"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "delicias_del_norte_barranca"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "delicias_del_norte_barranca_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "956847314"
//            )
//        )
//    ),
//
//    modelo_tienda_temporal(
//        nombre_tienda = "Copias Express Barranca",
//        modelo_negocio = true,
//        localidad = "barranca",
//        categoria_tienda = "comida y restaurantes",
//        descripcion = "Copias Express en barranca, especializado en pastelerias",
//        id_tienda = "55ea38dc-b515-402d-9",
//        img_perfil = "",
//        subcategoria = listOf("pastelerias"),
//        ubicacion = mapOf(
//            "latitud" to -10.732128,
//            "longitud" to -77.73007,
//            "dirección" to "Calle 30 Mz B Lt 3",
//            "referencia" to "Frente a parque San Juan"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "copias_express_barranca"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "copias_express_barranca_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "956372399"
//            )
//        )
//    ),
//
//    modelo_tienda_temporal(
//        nombre_tienda = "Look Urbano Barranca",
//        modelo_negocio = true,
//        localidad = "barranca",
//        categoria_tienda = "comida y restaurantes",
//        descripcion = "Look Urbano en barranca, especializado en pastelerias",
//        id_tienda = "226b0aec-3312-4e36-b",
//        img_perfil = "",
//        subcategoria = listOf("pastelerias"),
//        ubicacion = mapOf(
//            "latitud" to -10.603619,
//            "longitud" to -77.60594,
//            "dirección" to "Calle 43 Mz B Lt 5",
//            "referencia" to "Frente a parque Central"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "look_urbano_barranca"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "look_urbano_barranca_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "917731629"
//            )
//        )
//    ),
//
//    modelo_tienda_temporal(
//        nombre_tienda = "Estilo Total Barranca",
//        modelo_negocio = true,
//        localidad = "barranca",
//        categoria_tienda = "educacion y librerias",
//        descripcion = "Estilo Total en barranca, especializado en librerias",
//        id_tienda = "80f6fa59-46bc-4c65-b",
//        img_perfil = "",
//        subcategoria = listOf("librerias"),
//        ubicacion = mapOf(
//            "latitud" to -10.787396,
//            "longitud" to -77.624377,
//            "dirección" to "Calle 39 Mz F Lt 2",
//            "referencia" to "Frente a parque Las Flores"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "estilo_total_barranca"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "estilo_total_barranca_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "939778874"
//            )
//        )
//    ),
//
//    modelo_tienda_temporal(
//        nombre_tienda = "Sabor Criollo Barranca",
//        modelo_negocio = true,
//        localidad = "barranca",
//        categoria_tienda = "belleza",
//        descripcion = "Sabor Criollo en barranca, especializado en centros esteticos",
//        id_tienda = "20fd71f1-3bff-4e3f-8",
//        img_perfil = "",
//        subcategoria = listOf("centros esteticos"),
//        ubicacion = mapOf(
//            "latitud" to -10.609041,
//            "longitud" to -77.621474,
//            "dirección" to "Calle 8 Mz B Lt 3",
//            "referencia" to "Frente a parque San Juan"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sabor_criollo_barranca"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sabor_criollo_barranca_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "992376373"
//            )
//        )
//    ),
//
//
////paramonga
//    modelo_tienda(
//        nombre_tienda = "Sol y Belleza Paramonga",
//        modelo_negocio = true,
//        localidad = "paramonga",
//        categoria_tienda = "fotografia e impresion",
//        descripcion = "Sol y Belleza en paramonga, especializado en cabinas fotograficas",
//        id_tienda = "6f11e79e-452f-45c4-a",
//        img_perfil = "",
//        subcategoria = listOf("cabinas fotograficas"),
//        ubicacion = mapOf(
//            "latitud" to -10.617826,
//            "longitud" to -77.771605,
//            "dirección" to "Calle 31 Mz B Lt 9",
//            "referencia" to "Frente a parque Las Flores"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sol_y_belleza_paramonga"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sol_y_belleza_paramonga_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "922360884"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Delicias del Norte Paramonga",
//        modelo_negocio = true,
//        localidad = "paramonga",
//        categoria_tienda = "comida y restaurantes",
//        descripcion = "Delicias del Norte en paramonga, especializado en cafeterias",
//        id_tienda = "38a64ebc-23f0-4f3b-9",
//        img_perfil = "",
//        subcategoria = listOf("cafeterias"),
//        ubicacion = mapOf(
//            "latitud" to -10.625267,
//            "longitud" to -77.676666,
//            "dirección" to "Calle 2 Mz C Lt 9",
//            "referencia" to "Frente a parque Las Flores"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "delicias_del_norte_paramonga"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "delicias_del_norte_paramonga_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "972215170"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Copias Express Paramonga",
//        modelo_negocio = true,
//        localidad = "paramonga",
//        categoria_tienda = "belleza",
//        descripcion = "Copias Express en paramonga, especializado en barberias",
//        id_tienda = "02b13ec9-40e4-470d-b",
//        img_perfil = "",
//        subcategoria = listOf("barberias"),
//        ubicacion = mapOf(
//            "latitud" to -10.765929,
//            "longitud" to -77.687095,
//            "dirección" to "Calle 18 Mz C Lt 2",
//            "referencia" to "Frente a parque Las Flores"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "copias_express_paramonga"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "copias_express_paramonga_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "947749848"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Educando Futuro Paramonga",
//        modelo_negocio = true,
//        localidad = "paramonga",
//        categoria_tienda = "educacion y librerias",
//        descripcion = "Educando Futuro en paramonga, especializado en universidades privadas",
//        id_tienda = "132e81da-acb8-4429-a",
//        img_perfil = "",
//        subcategoria = listOf("universidades privadas"),
//        ubicacion = mapOf(
//            "latitud" to -10.763346,
//            "longitud" to -77.654293,
//            "dirección" to "Calle 31 Mz F Lt 8",
//            "referencia" to "Frente a parque San Juan"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "educando_futuro_paramonga"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "educando_futuro_paramonga_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "967359990"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Copias Express Paramonga",
//        modelo_negocio = true,
//        localidad = "paramonga",
//        categoria_tienda = "educacion y librerias",
//        descripcion = "Copias Express en paramonga, especializado en universidades privadas",
//        id_tienda = "ef715a3e-1f00-4b20-9",
//        img_perfil = "",
//        subcategoria = listOf("universidades privadas"),
//        ubicacion = mapOf(
//            "latitud" to -10.703363,
//            "longitud" to -77.775435,
//            "dirección" to "Calle 46 Mz F Lt 7",
//            "referencia" to "Frente a parque Las Flores"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "copias_express_paramonga"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "copias_express_paramonga_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "966566089"
//            )
//        )
//    ),
//
////pativilca
//    modelo_tienda(
//        nombre_tienda = "Sol y Belleza Supe",
//        modelo_negocio = true,
//        localidad = "supe",
//        categoria_tienda = "belleza",
//        descripcion = "Sol y Belleza en supe, especializado en peluquerias",
//        id_tienda = "38a5a1ef-8e4b-4776-8",
//        img_perfil = "",
//        subcategoria = listOf("peluquerias"),
//        ubicacion = mapOf(
//            "latitud" to -10.696236,
//            "longitud" to -77.741653,
//            "dirección" to "Calle 26 Mz C Lt 4",
//            "referencia" to "Frente a parque San Juan"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sol_y_belleza_supe"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sol_y_belleza_supe_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "937253243"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Café y Letras Supe",
//        modelo_negocio = true,
//        localidad = "supe",
//        categoria_tienda = "fotografia e impresion",
//        descripcion = "Café y Letras en supe, especializado en fotografias para dni y carnet",
//        id_tienda = "be1dad1a-4154-43dd-a",
//        img_perfil = "",
//        subcategoria = listOf("fotografias para dni y carnet"),
//        ubicacion = mapOf(
//            "latitud" to -10.747884,
//            "longitud" to -77.78035,
//            "dirección" to "Calle 8 Mz F Lt 10",
//            "referencia" to "Frente a parque Central"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "café_y_letras_supe"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "café_y_letras_supe_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "946433353"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Sabor Criollo Supe",
//        modelo_negocio = true,
//        localidad = "supe",
//        categoria_tienda = "fotografia e impresion",
//        descripcion = "Sabor Criollo en supe, especializado en fotografias para dni y carnet",
//        id_tienda = "5b28d9fc-b815-420e-8",
//        img_perfil = "",
//        subcategoria = listOf("fotografias para dni y carnet"),
//        ubicacion = mapOf(
//            "latitud" to -10.722951,
//            "longitud" to -77.735325,
//            "dirección" to "Calle 20 Mz D Lt 7",
//            "referencia" to "Frente a parque Las Flores"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sabor_criollo_supe"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sabor_criollo_supe_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "964206628"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Sabores del Perú Supe",
//        modelo_negocio = true,
//        localidad = "supe",
//        categoria_tienda = "fotografia e impresion",
//        descripcion = "Sabores del Perú en supe, especializado en plastificados y escaneos",
//        id_tienda = "0819630e-74d0-43f5-a",
//        img_perfil = "",
//        subcategoria = listOf("plastificados y escaneos"),
//        ubicacion = mapOf(
//            "latitud" to -10.651722,
//            "longitud" to -77.643936,
//            "dirección" to "Calle 32 Mz F Lt 8",
//            "referencia" to "Frente a parque Central"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sabores_del_perú_supe"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "sabores_del_perú_supe_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "912084859"
//            )
//        )
//    ),
//
//    modelo_tienda(
//        nombre_tienda = "Look Urbano Supe",
//        modelo_negocio = true,
//        localidad = "supe",
//        categoria_tienda = "comida y restaurantes",
//        descripcion = "Look Urbano en supe, especializado en pollerias",
//        id_tienda = "33aeafd0-ea29-4dce-a",
//        img_perfil = "",
//        subcategoria = listOf("pollerias"),
//        ubicacion = mapOf(
//            "latitud" to -10.762486,
//            "longitud" to -77.74658,
//            "dirección" to "Calle 32 Mz F Lt 8",
//            "referencia" to "Frente a parque Las Flores"
//        ),
//        metodo_contacto = mapOf(
//            "facebook" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "look_urbano_supe"
//            ),
//            "instagram" to mapOf(
//                "estado" to true,
//                "nombre_buscador" to "look_urbano_supe_ig"
//            ),
//            "whatsapp" to mapOf(
//                "estado" to true,
//                "numero" to "921267026"
//            )
//        )
//    )
//)