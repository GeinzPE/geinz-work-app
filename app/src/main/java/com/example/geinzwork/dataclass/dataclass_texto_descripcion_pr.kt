package com.example.geinzwork.dataclass

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class dataclass_texto_descripcion_pr(
    val titulo_descripcion: String,
    val valor_boldtexto_titulo: String,
    val minusmayus_titulo: String,
    val descripcion_texto: String,
    val valor_boldtexto_texto: String,
    val minusmayus_titulo_texto: String,
    val listaEncontrados: List<String>
)
class MiViewModel : ViewModel() {
    val datosDescripcion = MutableLiveData<dataclass_texto_descripcion_pr>()
}

