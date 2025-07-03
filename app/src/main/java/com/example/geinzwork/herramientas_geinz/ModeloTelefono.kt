package com.example.geinzwork.herramientas_geinz

data class ModeloTelefono(

    var marca: String="",
    var series: String = "",
    var codigos_internos: List<String> = emptyList(), // Lista de cadenas
)
