package com.geinzz.geinzwork.viewModels

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.data.model.datos_cambiar_cat_sub
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia_bloques
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_tienda
import com.geinzz.geinzwork.model.repo_agregar_datos
import com.geinzz.geinzwork.model.repo_filtrado_tiendas
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.HorasDia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.toMetodoContacto
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_horario_atencion_box_dia
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.to_metodo_pago
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class viewmodel_agregar_datos : ViewModel() {

    val dias = listOf(
        "Lunes", "Martes", "Miércoles",
        "Jueves", "Viernes", "Sábado", "Domingo"
    )

    val mapaHoras = dias.associateWith { HorasDia() }.toMutableMap()

    val _cat_sub_tienda = MutableStateFlow(
        datos_cambiar_cat_sub(
            nombre_lugar = "",
            pertenerce_algolia = false,
            esta_nuevo = false,
            cat = "",
            lista_sub = emptyList()
        )
    )

    val obtener_cat_sub_tienda: StateFlow<datos_cambiar_cat_sub> = _cat_sub_tienda


    fun obtenerHorarioAtencion(): HorarioAtencion_box {

        fun crearBloques(h: HorasDia): List<HorarioBloque> {

            val bloques = mutableListOf<HorarioBloque>()

            // Bloque AM
            if (h.h1AM.value.isNotBlank() && h.h2AM.value.isNotBlank()) {
                bloques.add(
                    HorarioBloque(
                        h_apertura = h.h1AM.value,
                        h_cierre = h.h2AM.value
                    )
                )
            }

            // Bloque PM
            if (h.h1PM.value.isNotBlank() && h.h2PM.value.isNotBlank()) {
                bloques.add(
                    HorarioBloque(
                        h_apertura = h.h1PM.value,
                        h_cierre = h.h2PM.value
                    )
                )
            }

            return bloques
        }

        return HorarioAtencion_box(
            lunes = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Lunes"]!!),
                cerrado = !mapaHoras["Lunes"]!!.cerrado.value,
                motivo = "" // si tienes motivo, lo pones aquí
            ),
            martes = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Martes"]!!),
                cerrado = !mapaHoras["Martes"]!!.cerrado.value,
                motivo = ""
            ),
            miércoles = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Miércoles"]!!),
                cerrado = !mapaHoras["Miércoles"]!!.cerrado.value,
                motivo = ""
            ),
            jueves = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Jueves"]!!),
                cerrado = !mapaHoras["Jueves"]!!.cerrado.value,
                motivo = ""
            ),
            viernes = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Viernes"]!!),
                cerrado = !mapaHoras["Viernes"]!!.cerrado.value,
                motivo = ""
            ),
            sábado = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Sábado"]!!),
                cerrado = !mapaHoras["Sábado"]!!.cerrado.value,
                motivo = ""
            ),
            domingo = HorarioDia_bloques(
                bloques = crearBloques(mapaHoras["Domingo"]!!),
                cerrado = !mapaHoras["Domingo"]!!.cerrado.value,
                motivo = ""
            )
        )
    }

    suspend fun obtenerDireccion(lat: Double, lon: Double, context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val direcciones = geocoder.getFromLocation(lat, lon, 1)

                if (!direcciones.isNullOrEmpty()) {
                    val addressLine = direcciones[0].getAddressLine(0)

                    Log.d("GeocoderFunc", "AddressLine(0): $addressLine")

                    addressLine
                } else {
                    Log.w("GeocoderFunc", "No se encontró AddressLine(0)")
                    null
                }
            } catch (e: Exception) {
                Log.e("GeocoderFunc", "Error: ${e.message}")
                null
            }
        }
    }

    fun obtener_cat_sub_tienda(
        id_tienda: String,
        localidad_tienda: String,
        context: Context
    ) {
        val repo_instance = repo_agregar_datos(context)

        Log.d("CAT_SUB_DEBUG", "🔍 Iniciando búsqueda...")
        Log.d("CAT_SUB_DEBUG", "📌 ID tienda: $id_tienda")
        Log.d("CAT_SUB_DEBUG", "📌 Localidad: $localidad_tienda")

        viewModelScope.launch {

            try {
                val datos = repo_instance.obtener_cat_sub_tienda(id_tienda, localidad_tienda)

                Log.d("CAT_SUB_DEBUG", "✅ Resultado obtenido:")
                Log.d("CAT_SUB_DEBUG", "➡ Nombre: ${datos.nombre_lugar}")
                Log.d("CAT_SUB_DEBUG", "➡ Algolia: ${datos.pertenerce_algolia}")
                Log.d("CAT_SUB_DEBUG", "➡ Nuevo: ${datos.esta_nuevo}")
                Log.d("CAT_SUB_DEBUG", "➡ Categoría: ${datos.cat}")
                Log.d("CAT_SUB_DEBUG", "➡ Subcategorías: ${datos.lista_sub}")

                _cat_sub_tienda.value = datos

            } catch (e: Exception) {
                Log.e("CAT_SUB_DEBUG", "❌ ERROR obteniendo datos:", e)

                _cat_sub_tienda.value = datos_cambiar_cat_sub(
                    nombre_lugar = "",
                    pertenerce_algolia = false,
                    esta_nuevo = false,
                    cat = "",
                    lista_sub = emptyList()
                )
            }
        }
    }

    fun guaradr_cat_sub_nueva(
        algolia: Boolean,nuevos_negocios: Boolean,
        id_tienda: String,
        localidad_tienda: String,
        context: Context,
        cat: String,
        sub: List<String>,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
        val repo_instance = repo_agregar_datos(context)

            try {
                repo_instance.guardar_datos_tienda(
                    algolia,nuevos_negocios,
                    id_tienda = id_tienda,
                    localidad = localidad_tienda,
                    cat = cat,
                    subcat = sub
                ) { ok ->
                    onResult(ok)
                }
            } catch (e: Exception) {
                Log.e("guaradr_cat_sub_nueva", "❌ Error al guardar $e")
                onResult(false)
            }
        }
    }

    fun limpiarCatSubTienda() {
        _cat_sub_tienda.value = datos_cambiar_cat_sub(
            cat = "",
            lista_sub = emptyList(),
            nombre_lugar = "",
            pertenerce_algolia = false,
            esta_nuevo = false
        )
    }






}