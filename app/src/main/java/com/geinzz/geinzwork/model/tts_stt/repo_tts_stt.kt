package com.geinzz.geinzwork.model.tts_stt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class repo_tts_stt {
    private val client = OkHttpClient()
    suspend fun cloudTTS(
        texto: String,
        voz: String
    ): ByteArray =
        withContext(Dispatchers.IO) {

            val json = JSONObject()
            json.put("texto", texto)
            json.put("voz", voz)

            val body = json.toString()
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://us-central1-geinzworkapp.cloudfunctions.net/textToSpeechIA_con_params")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("Error TTS: ${response.code}")
            }

            response.body?.bytes()
                ?: throw IOException("Audio vacío")
        }


}