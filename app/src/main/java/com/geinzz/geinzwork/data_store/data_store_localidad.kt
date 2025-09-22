package com.geinzz.geinzwork.data_store

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name="settings")
object data_store_localidad {
    private val LOCALIDAD_KEY = stringPreferencesKey("Ulitma_localidad")

    suspend fun guardar_localida(context: Context,nombre: String){
        context.dataStore.edit { preferences ->
            preferences[LOCALIDAD_KEY]=nombre
        }
        Log.d("guadmos_localida",nombre)
    }

    fun obtener_localidad(context: Context): Flow<String?>{
        return context.dataStore.data.map { preferences ->
            preferences[LOCALIDAD_KEY]
        }
    }
}