package com.geinzz.geinzwork.retrofit

import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.DatosResponse
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ResAlgoliaFiltrado
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.RespuestaGemini
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.TextoRequest
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_envidiadosbody_algolia
import retrofit2.http.Body
import retrofit2.http.POST

interface retrofit_api {
    @POST("extraerDatos")
    suspend fun extraerDatos(
        @Body request: TextoRequest
    ): DatosResponse


    @POST("filtrar_por_datos")
    suspend fun Consultar_algolia(@Body request: DatosResponse): ResAlgoliaFiltrado
}