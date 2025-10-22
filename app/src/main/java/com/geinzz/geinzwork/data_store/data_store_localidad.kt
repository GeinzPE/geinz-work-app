package com.geinzz.geinzwork.data_store

import android.R
import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

object data_store_localidad {
    private val LOCALIDAD_KEY = stringPreferencesKey("Ulitma_localidad")
    private val NOTIFICACIONES_KEY = booleanPreferencesKey("notificacion_ed")
    private val MOSTRAR_DIALOG_NOTI = booleanPreferencesKey("notifi_dialog")

    private val RADIO_USER_KEY = floatPreferencesKey("radio_user_value")


    suspend fun guardar_localida(context: Context, nombre: String) {
        context.dataStore.edit { preferences ->
            preferences[LOCALIDAD_KEY] = nombre
        }
        Log.d("guadmos_localida", nombre)
    }

    fun obtener_localidad(context: Context): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[LOCALIDAD_KEY]
        }
    }

    suspend fun sendNotificacion(context: Context, value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICACIONES_KEY] = value
        }
    }

    fun getNotificacion(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { pref ->
            pref[NOTIFICACIONES_KEY] ?: false
        }
    }

    suspend fun guarar_dialogo_notifi(context: Context, value: Boolean) {
        context.dataStore.edit { pref ->
            pref[MOSTRAR_DIALOG_NOTI] = value
        }
    }

    fun get_dialog_notifi(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { pref ->
            pref[MOSTRAR_DIALOG_NOTI] ?: false
        }
    }

    suspend fun guardar_radio_user(context: Context, radio: Float) {
        Log.d("guarmod_radio","$radio")
        context.dataStore.edit { preferences ->
            preferences[RADIO_USER_KEY] = radio
        }
    }

    fun get_radio_user(context: Context): Flow<Float> {
        return context.dataStore.data.map { pref ->
            pref[RADIO_USER_KEY] ?: 1f
        }
    }
}