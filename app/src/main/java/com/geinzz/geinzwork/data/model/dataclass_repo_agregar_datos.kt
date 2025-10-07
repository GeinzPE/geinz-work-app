package com.geinzz.geinzwork.data.model


data class dataclass_repo_agregar_datos(
    var nombre_lugar: String,
    val lat: Double,
    val long: Double,
    val numero_telefono: Int
)

data class dataclass_lugares_db(
    var categoria: List<String> = emptyList(),
    var direccion: direccion_lugar = direccion_lugar(),
    val horario_atencion: Map<String, Any> = emptyMap(),
    val id: String = "",
    val lugar_nombre: String = "",
    var logo_img: String = "",
    val contacto: String=""
)


data class direccion_lugar(
    val lat: Double = 0.0,
    val log: Double = 0.0,
    val direccion: String = "",
    val refencia: String = ""
)