package com.geinzz.geinzwork.model
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_promp_NLP_depromo_y_oferta
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_promp_para_Descripcion
import com.geinzz.geinzwork.herramientas_geinz.constantes.construir_prompt_para_titulo_casa
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class repo_agregar_inmubles {

    suspend fun generar_titulo_propiedad(
        tipo_realizado: String,
        tipo_operacion: String,
        nombre_Calle: String,
        localidad: String
    ): String? {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")
        val prompt = construir_prompt_para_titulo_casa(tipo_realizado, tipo_operacion,nombre_Calle,localidad)
        val result = model.generateContent(prompt)
        return result.text
    }

    suspend fun generar_descripcion(titulo: String, lista_lugares: List<String>): String? {
        val model = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash")
        val prompt = construir_promp_para_Descripcion(titulo, lista_lugares)
        val result = model.generateContent(prompt)
        return result.text
    }


}