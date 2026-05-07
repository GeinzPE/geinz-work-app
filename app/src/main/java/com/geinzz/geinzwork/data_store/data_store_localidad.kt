package com.geinzz.geinzwork.data_store

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.geinzz.geinzwork.data.model.dataclass_seguridad.FrasePendiente
import com.google.common.reflect.TypeToken
import com.google.firebase.Timestamp
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.IOException
import kotlin.text.set

val Context.dataStore by preferencesDataStore(name = "settings")

object data_store_localidad {
    private val LOCALIDAD_KEY = stringPreferencesKey("Ulitma_localidad")
    private val NOTIFICACIONES_KEY = booleanPreferencesKey("notificacion_ed")
    private val MOSTRAR_DIALOG_NOTI = booleanPreferencesKey("notifi_dialog")

    private val RADIO_USER_KEY = floatPreferencesKey("radio_user_value")
    private val HASHING_USER_KEY = stringPreferencesKey("hashing_user")

    private val LATITUD_USER_KEY = doublePreferencesKey("latitud")
    private val LONGITUD_USER_KEY = doublePreferencesKey("longitud")
    private val HORA_HASHING_USER_KEY = stringPreferencesKey("hora_hashing_user")

    val UID_USER_REGISTER = stringPreferencesKey("uid_user_register")
    private val EMAIL_USER_REGISTER = stringPreferencesKey("email_user_register")

    private val KEY_URLS_salud = stringPreferencesKey("urls_carga_salud")

    private val KEY_URLS_turistmo = stringPreferencesKey("urls_carga_turismo")

    private val KEY_URLS_FILTRADO_LOC = stringPreferencesKey("urls_carga_filtrado_loc")


    private val KEY_ID_SOCIO = stringPreferencesKey("id_key_socio")

    private val LOCALIDAD_SOCIO_TIENDA =stringPreferencesKey("localidad_tienda_socio")

    val FRASES_KEY = stringPreferencesKey("frases_pendientes")

    val APERTURA_APARTADO_KEY = intPreferencesKey("apertura_apartado")


    suspend fun resetearContador(context: Context) {
        context.dataStore.edit { prefs ->
            prefs[APERTURA_APARTADO_KEY] = 0
        }
    }
    suspend fun limpiarFrasesLocales(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(FRASES_KEY)
        }
    }


    suspend fun incrementarAperturaApartado(context: Context) {
        context.dataStore.edit { prefs ->
            val actual = prefs[APERTURA_APARTADO_KEY] ?: 0
            prefs[APERTURA_APARTADO_KEY] = actual + 1
        }
    }

    suspend fun obtenerCantidadAperturas(context: Context): Int {
        val prefs = context.dataStore.data.first()
        return prefs[APERTURA_APARTADO_KEY] ?: 0
    }


    suspend fun obtenerFrases(context: Context): List<FrasePendiente> {

        val prefs = context.dataStore.data.first()
        val json = prefs[FRASES_KEY] ?: return emptyList()

        val type = object : TypeToken<List<FrasePendiente>>() {}.type
        return Gson().fromJson(json, type)
    }

    suspend fun guardarFraseNoReconocida(context: Context, i:FrasePendiente) {

        val nuevaFrase = FrasePendiente(
            texto = i.texto,
            accion = i.accion,
            termino = i.termino,
            salud_o_sec = i.salud_o_sec,
            categoriazacion = i.categoriazacion
        )

        val listaActual = obtenerFrases(context).toMutableList()
        listaActual.add(nuevaFrase)

        val json = Gson().toJson(listaActual)

        context.dataStore.edit { prefs ->
            prefs[FRASES_KEY] = json
        }
    }



    suspend fun set_id_socio(context: Context, id: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ID_SOCIO] = id
        }
    }

    suspend fun set_localidad_tienda_soscio(context: Context, localidad_tienda:String){
        context.dataStore.edit { preferences ->

            preferences[LOCALIDAD_SOCIO_TIENDA]=localidad_tienda
        }
    }

    fun get_id_socio(context: Context): Flow<String>{
        return context.dataStore.data.map { preferences ->
            preferences[KEY_ID_SOCIO] ?:""
        }
    }
    fun get_localidad_tienda_socio(context: Context): Flow<String>{
        return context.dataStore.data.map { preferences ->
            preferences[LOCALIDAD_SOCIO_TIENDA] ?:""
        }
    }

    suspend fun delete_id_socio(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ID_SOCIO)
        }
    }



    suspend fun guardarUrlsCarga(context: Context, lista: List<String>) {
        val json = JSONArray(lista).toString()
        context.dataStore.edit { prefs ->
            prefs[KEY_URLS_salud] = json
        }
    }

    suspend fun guardarUrlsCarga_turismo(context: Context, lista: List<String>) {
        val json = JSONArray(lista).toString()
        context.dataStore.edit { prefs ->
            prefs[KEY_URLS_turistmo] = json
        }
    }

    suspend fun guardarUrlsCarga_filtrado(context: Context, lista: List<String>) {
        val json = JSONArray(lista).toString()
        context.dataStore.edit { prefs ->
            prefs[KEY_URLS_FILTRADO_LOC] = json
        }
    }

    suspend fun obtenerUrlsCarga_filtrado(context: Context): List<String> {
        val prefs = context.dataStore.data.first()
        val json = prefs[KEY_URLS_FILTRADO_LOC] ?: return emptyList()

        val array = JSONArray(json)
        return List(array.length()) { i -> array.getString(i) }
    }

    suspend fun obtenerUrlsCarga_turismo(context: Context): List<String> {
        val prefs = context.dataStore.data.first()
        val json = prefs[KEY_URLS_turistmo] ?: return emptyList()

        val array = JSONArray(json)
        return List(array.length()) { i -> array.getString(i) }
    }

    suspend fun obtenerUrlsCarga(context: Context): List<String> {
        val prefs = context.dataStore.data.first()
        val json = prefs[KEY_URLS_salud] ?: return emptyList()

        val array = JSONArray(json)
        return List(array.length()) { i -> array.getString(i) }
    }

    suspend fun guardar_datos_user(context: Context, uid: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[UID_USER_REGISTER] = uid
            preferences[EMAIL_USER_REGISTER] = email
        }
    }

    fun get_uid_user(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[UID_USER_REGISTER] ?: ""
        }
    }

    fun get_email_user(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[EMAIL_USER_REGISTER] ?: ""
        }
    }

    suspend fun limpiar_datos_autenticacion(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.remove(UID_USER_REGISTER)
            preferences.remove(EMAIL_USER_REGISTER)
            preferences.remove(KEY_ID_SOCIO)
            preferences.remove(LOCALIDAD_SOCIO_TIENDA)
        }
        Log.d("DataStore", "UID y email eliminados")
    }


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
        Log.d("datapara_ver","$MOSTRAR_DIALOG_NOTI")
        return context.dataStore.data.map { pref ->
            pref[MOSTRAR_DIALOG_NOTI] ?: false
        }
    }

    suspend fun guardar_radio_user(context: Context, radio: Float) {
        Log.d("guarmod_radio", "$radio")
        context.dataStore.edit { preferences ->
            preferences[RADIO_USER_KEY] = radio
        }
    }

    fun get_radio_user(context: Context): Flow<Float> {
        return context.dataStore.data.map { pref ->
            pref[RADIO_USER_KEY] ?: 1f
        }
    }

    suspend fun guardar_hasgin_lat_lon_user(context: Context, hashin: String, hora: String) {
        Log.d("hasing_user_guardo", "$hashin $hora")
        context.dataStore.edit { preferences ->
            preferences[HASHING_USER_KEY] = hashin
            preferences[HORA_HASHING_USER_KEY] = hora
        }
        Log.d("guardar_hashing", "Hash: $hashin - Hora: $hora")
    }

    fun obtener_hashing_user(context: Context): Flow<String?> {
        return context.dataStore.data.map { pref ->
            pref[HASHING_USER_KEY] ?: ""
        }

    }


    suspend fun guardar_lat_log_user(context: Context, lat: Double, long: Double) {
        context.dataStore.edit { preferences ->
            preferences[LATITUD_USER_KEY] = lat
            preferences[LONGITUD_USER_KEY] = long
        }

        Log.d("DataStoreUser", "Guardado -> Lat: $lat | Lon: $long")
    }


    fun obtenerLatLonUsuario(context: Context): Flow<Pair<Double, Double>> {
        return context.dataStore.data
            .map { preferences ->
                Pair(
                    preferences[LATITUD_USER_KEY] ?: 0.0,
                    preferences[LONGITUD_USER_KEY] ?: 0.0
                )
            }
    }




    fun get_hora_hashin_user(context: Context): Flow<String?> {
        return context.dataStore.data.map { pref ->
            pref[HORA_HASHING_USER_KEY]
        }
    }
}