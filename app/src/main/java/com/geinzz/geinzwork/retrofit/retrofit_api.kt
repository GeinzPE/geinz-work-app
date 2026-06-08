package com.geinzz.geinzwork.retrofit

import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.DatosResponse
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ResAlgoliaFiltrado
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.ResAlgoliaFiltrado_manual
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.RespuestaGemini
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.TextoRequest
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_envidiadosbody_algolia
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.datos_para_filtrado_manual
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface retrofit_api {
    @POST("extraerDatos")
    suspend fun extraerDatos(
        @Body request: TextoRequest
    ): DatosResponse


    @POST("filtrar_por_datos")
    suspend fun Consultar_algolia(@Body request: DatosResponse): ResAlgoliaFiltrado

    @POST("obtener_filtrado_manual_alogolia")
    suspend fun construir_filtrado_manual(@Body request: datos_para_filtrado_manual): ResAlgoliaFiltrado

    @Multipart
    @POST("https://transcribiraudio-oixttik5rq-uc.a.run.app") // tu URL de cloud function
    suspend fun transcribirAudio(
        @Part audio: MultipartBody.Part
    ): TranscripcionResponse

    data class TranscripcionResponse(val texto: String)
}