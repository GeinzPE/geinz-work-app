package com.geinzz.geinzwork.viewModels.tts_stt

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.model.tts_stt.repo_tts_stt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class tts_stt: ViewModel() {
    private var mediaPlayer: MediaPlayer? = null
    val insta_tts= repo_tts_stt()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _datosCloudTts = MutableStateFlow(ByteArray(0))
    val datosCloudTts: StateFlow<ByteArray> = _datosCloudTts
    fun reproducirMP3(context: Context, audioBytes: ByteArray) {
        try {

            if (audioBytes.isEmpty()) {
                Log.e("TTS", "Audio vacío")
                return
            }

            // 🔥 PONLO AQUÍ
            Log.d("TTS_SIZE", "Bytes recibidos: ${audioBytes.size}")

            val tempFile = File.createTempFile("tts_", ".mp3", context.cacheDir)
            tempFile.writeBytes(audioBytes)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                prepare()
                start()
                _isPlaying.value = true


                setOnCompletionListener {
                    it.release()
                    tempFile.delete()
                    mediaPlayer = null
                    _isPlaying.value = false
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e("TTS", "Error en MediaPlayer: $what / $extra")
                    mp.release()
                    tempFile.delete()
                    mediaPlayer = null
                    _isPlaying.value = false
                    true
                }
            }

        } catch (e: Exception) {
            Log.e("TTS", "Error reproduciendo MP3", e)
        }
    }
    fun limpiarAudio() {
        _datosCloudTts.value = ByteArray(0)
    }

    fun crear_texto__para_tts(texto: String,tipo_voz:String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _datosCloudTts.value = insta_tts.cloudTTS(texto,tipo_voz)
            } catch (e: Exception) {
                Log.e("CloudTTS", "Error de text to speech", e)
            }
        }
    }

    fun detenerAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("TTS", "Error al detener audio", e)
        }

        _isPlaying.value = false
        _datosCloudTts.value = ByteArray(0)
    }
}