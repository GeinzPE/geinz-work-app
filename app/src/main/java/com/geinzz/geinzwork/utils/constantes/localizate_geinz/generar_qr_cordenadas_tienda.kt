package com.geinzz.geinzwork.utils.constantes.localizate_geinz


import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object generar_qr_cordenadas_tienda {

    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128


    fun generae_Key(): SecretKey {
        val key_gen = KeyGenerator.getInstance("AES")
        key_gen.init(AES_KEY_SIZE)
        return key_gen.generateKey()
    }

    fun cifrado_UBi(cordenadas: String, key_secret: SecretKey): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(GCM_IV_LENGTH).also { java.security.SecureRandom().nextBytes(it) }
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key_secret, spec)
        val ciPherByte = cipher.doFinal(cordenadas.toByteArray(Charsets.UTF_8))

        val combined = ByteArray(iv.size + ciPherByte.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciPherByte, 0, combined, iv.size, ciPherByte.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }


    fun desencriptado(base64Combined: String, secretKey: SecretKey): String {
        val combined = Base64.decode(base64Combined, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val cipherBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val plain = cipher.doFinal(cipherBytes)
        return String(plain, Charsets.UTF_8)
    }

    fun codificarCoordenadas(lat: Double, lng: Double): String {
            Log.d("lat_dog","${lat} ${lng}")
        val coordenadasCodificadas = Base64.encodeToString("$lat,$lng".toByteArray(), Base64.NO_WRAP)
        return "Review|$coordenadasCodificadas"
    }

    fun retornar_id_Tienda_lugar(id: String): String{
        return "Review|$id"
    }

    fun decodificarCoordenadas(data: String): Pair<Double, Double> {
        val decoded = String(Base64.decode(data, Base64.DEFAULT))
        val partes = decoded.split(",")
        return Pair(partes[0].toDouble(), partes[1].toDouble())
    }

}