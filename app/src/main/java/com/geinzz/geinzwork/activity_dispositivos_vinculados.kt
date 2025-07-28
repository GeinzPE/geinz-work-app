package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.geinzz.geinzwork.ui.adapters.adapter_dispo_vinculados
import com.geinzz.geinzwork.utils.constantes.constantes.PinShortcut_general
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_vinculados.encontrarUser
import com.geinzz.geinzwork.utils.constantes.constantes.constantes_vinculados.obtenerAndroidID
import com.geinzz.geinzwork.model.dataclass_dispo_vinculados
import com.geinzz.geinzwork.utils.constantes.constantes.constantestextos_general
import com.geinzz.geinzwork.databinding.ActivityDispositivosVinculadosBinding
import com.geinzz.geinzwork.databinding.BottomSheetCerraSeccionConfirBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.geinzz.geinzwork.databinding.BottomSheetUserCrrsVincualdosBinding
import com.geinzz.geinzwork.databinding.BottomSheetlCrrsVinculadosBinding

import android.provider.Settings
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.geinzz.geinzwork.viewModels.viewModel_dispo_vincualdos
import com.geinzz.geinzwork.viewModels.viewModel_notificaciones
import com.geinzz.geinzwork.viewModels.viewModel_text_validaciones
import com.geinzz.geinzwork.viewModels.viewModel_usuarios_general
import kotlinx.coroutines.launch


class activity_dispositivos_vinculados : AppCompatActivity() {
    private lateinit var binding: ActivityDispositivosVinculadosBinding
    private val lista = mutableListOf<dataclass_dispo_vinculados>()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private var vmNotificaciones: viewModel_notificaciones? = null
    private var vmTextosValidaciones: viewModel_text_validaciones? = null
    private var vmDispoVinculados: viewModel_dispo_vincualdos? = null
    private var vmUsuariosGenerales: viewModel_usuarios_general? = null
    private var primario_boolean: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDispositivosVinculadosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            vmNotificaciones = ViewModelProvider(this)[viewModel_notificaciones::class.java]
            vmTextosValidaciones = ViewModelProvider(this)[viewModel_text_validaciones::class.java]
            vmDispoVinculados = ViewModelProvider(this)[viewModel_dispo_vincualdos::class.java]
            vmUsuariosGenerales = ViewModelProvider(this)[viewModel_usuarios_general::class.java]
            binding.swipe.isVisible = true
            vmDispoVinculados?.obtener_vinculados()

            lifecycleScope.launch {
                vmDispoVinculados?.obtener_dispo_vinculados?.collect { lista_vincualdos ->
                    if (lista_vincualdos.isNotEmpty()) {
                        binding.recicleDispositivos.isVisible = true
                        binding.LinealCargaDispo.isVisible = false
                        binding.swipe.isVisible = true
                        binding.noDispositivo.isVisible = false
                        val recicle = binding.recicleDispositivos
                        recicle.layoutManager =
                            LinearLayoutManager(this@activity_dispositivos_vinculados)
                        vmDispoVinculados?.puedeCerrarSesionDispositivo(firebaseAuth.uid.toString())
                        recicle.adapter =
                            adapter_dispo_vinculados(lista_vincualdos as MutableList<dataclass_dispo_vinculados>) { idDispoSeleccionado ->
                                val androidId =
                                    obtenerAndroidID(this@activity_dispositivos_vinculados)
                                vmUsuariosGenerales?.encontra_user(firebaseAuth.uid.toString())
                                manejarCerrarSesion(idDispoSeleccionado, androidId)
                            }
                    } else {
                        binding.noDispositivo.isVisible = true
                        binding.recicleDispositivos.isVisible = false
                        binding.LinealCargaDispo.isVisible = false
                        binding.swipe.isVisible = false
                    }

                }
            }
//            vmUsuariosGenerales?.encontra_user(firebaseAuth.uid.toString())
//            vmDispoVinculados?.buscar_primario?.observe(this@activity_dispositivos_vinculados) { existe ->
//                if (existe.isNotEmpty()) {
//                    dialog = BottomSheetDialog(this)
//                    dialog_cerrar_seccion(
//                        "it.nombre_dispo.toString()",
//                        "it.id_dispo.toString()",
//                        true
//                    )
//                    dialog.show()
//                } else {
//                    dialog = BottomSheetDialog(this)
//                    dialog_cerrar_seccion(
//                        "it.nombre_dispo.toString()",
//                        "it.id_dispo.toString()",
//                        false
//                    )
//                    dialog.show()
//
//                }
//                datos?.let {
//                    Log.d(
//                        "DEBUG_DISPO", """
//    primarioExiste: ${it.primarioExiste}
//    cerrarSeccionMismoDispo: ${it.cerrarSeccionMismoDispo}
//    soyPrimario: ${it.soyPrimario}
//    esDispositivoActual: ${it.esDispositivoActual}
//    esPrimarioActual: ${it.esPrimarioActual}
//    nombre_dispo: ${it.nombre_dispo}
//    id_dispo: ${it.id_dispo}
//""".trimIndent()
//                    )
//                    when {
//
//                        // CASO 1: Es primario y se quiere cerrar a sí mismo
//                        it.primarioExiste && it.cerrarSeccionMismoDispo -> {
//                            Toast.makeText(
//                                this,
//                                "No puedes cerrar sesión desde aquí",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//
//                        // CASO 2: Es primario y quiere cerrar otro dispositivo
//                        it.primarioExiste && !it.cerrarSeccionMismoDispo -> {
//                            dialog = BottomSheetDialog(this)
//                            dialog_cerrar_seccion(
//                                it.nombre_dispo.toString(),
//                                it.id_dispo.toString(),
//                                true
//                            )
//
//                            dialog.show()
//                            Log.d("verificamosdatos", "${it.id_dispo}")
//                        }
//
//                        // CASO 3: No hay ningún primario → todos pueden cerrarse
//                        !it.primarioExiste -> {
//                            dialog = BottomSheetDialog(this)
//                            dialog_cerrar_seccion(
//                                it.nombre_dispo.toString(),
//                                it.id_dispo.toString(),
//                                false
//                            )
//                            dialog.show()
//                            Log.d("verificamosdatos", "${it.id_dispo}")
//                        }
//                    }
//                }
//            }


//            obtener_dispositivos_vinculados()
            confSwipe()
            binding.sinRegistro.isVisible = false
        } else {
            binding.sinRegistro.isVisible = true
            binding.swipe.isVisible = false
            binding.cargaDispo.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "dispositivos")
                }
                startActivity(vista)
            }
        }

        binding.popup.setOnClickListener {
            popup()
        }

    }

    private fun manejarCerrarSesion(
        idDispoSeleccionado: dataclass_dispo_vinculados,
        androidId: String
    ) {

        vmDispoVinculados?.buscar_primario?.observeOnce(this) { idPrimario ->

            if (idPrimario.isNotEmpty()) {
                if (idPrimario == idDispoSeleccionado.id_dispo && idPrimario == androidId) {
                    Log.d("validamos_campos1", "$idPrimario == ${idDispoSeleccionado.id_dispo}")
                    Toast.makeText(
                        this,
                        "No puedes cerrar sesión directamente desde este dispositivo",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (idPrimario != androidId) {
                    Log.d("validamos_campos2", "$idPrimario == ${androidId}")

                    Toast.makeText(
                        this,
                        "Solo el dispositivo primario puede cerrar sesión en otros dispositivos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    dialog = BottomSheetDialog(this)
                    dialog_cerrar_seccion(
                        idDispoSeleccionado.nombre_dispo.toString(),
                        idDispoSeleccionado.id_dispo.toString(),
                        true
                    )
                    dialog.show()
                }
            } else {
                dialog = BottomSheetDialog(this)
                dialog_cerrar_seccion(
                    idDispoSeleccionado.nombre_dispo.toString(),
                    idDispoSeleccionado.id_dispo.toString(),
                    false
                )
                dialog.show()
            }
        }
    }

    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T) -> Unit) {
        val obs = object : Observer<T> {
            override fun onChanged(t: T) {
                observer(t)
                removeObserver(this)
            }
        }
        observe(lifecycleOwner, obs)
    }


    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser != null) {
            binding.swipe.isVisible = true
            Toast.makeText(this, "entramos a la actividad", Toast.LENGTH_SHORT).show()
            confSwipe()
            binding.sinRegistro.isVisible = false
        } else {
            binding.sinRegistro.isVisible = true
            binding.swipe.isVisible = false
            binding.cargaDispo.isVisible = false
            binding.iniciarSeccion.setOnClickListener {
                val vista = Intent(this, Login::class.java).apply {
                    putExtra("dato", "dispositivos")
                }
                startActivity(vista)
            }
        }

    }

    private fun popup() {
        val popup = PopupMenu(this, binding.popup)
        popup.menu.add(Menu.NONE, 1, 1, "Crear acceso directo")
        popup.show()
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    PinShortcut_general.vinculados_accesoDirecto_panel(this)
                    true
                }

                else -> true
            }
        }
    }

//    private fun obtener_dispositivos_vinculados() {
//        val handler = android.os.Handler(android.os.Looper.getMainLooper())
//        val startTime = System.currentTimeMillis()  // Tiempo de inicio
//
//        encontrarUser(firebaseAuth.uid.toString()) { tipo, coleccion ->
//            when (tipo) {
//                "trabajador" -> {
//                    val db = FirebaseFirestore.getInstance()
//                        .collection("Trabajadores_Usuarios_Drivers")
//                        .document("trabajadores").collection("trabajadores")
//                        .document(firebaseAuth.uid.toString()).collection("vinculados")
//
//                    db.get().addOnSuccessListener { res ->
//                        val spannableString =
//                            SpannableString("Disposivos vinculados : ${res.size().toString()}")
//                        constantestextos_general.setearInformacionboldDescripcion(
//                            "Disposivos vinculados",
//                            spannableString, binding.TotalDispo
//                        )
//                        lista.clear()
//                        for (datos in res) {
//                            val data = datos.data
//                            val dispositivo = data?.get("dispositivo") as? String ?: ""
//                            val fecha_registro = data?.get("fecha_registro") as? String ?: ""
//                            val hora_registro = data?.get("hora_registro") as? String ?: ""
//                            val id_dispositivo = data?.get("id_dispositivo") as? String ?: ""
//                            val primario = data?.get("primario") as? Boolean ?: false
//                            val ultima_con = data?.get("ultima_con") as? String ?: ""
//                            val untima_fecha_con = data?.get("untima_fecha_con") as? String ?: ""
//                            val marca = obtenerMarcaDesdeModelo(dispositivo)
//
//                            val datos = dataclass_dispo_vinculados(
//                                id_dispositivo,
//                                dispositivo,
//                                hora_registro,
//                                fecha_registro, marca, primario, untima_fecha_con, ultima_con
//                            )
//                            lista.add(datos)
//                        }
//                        val endTime = System.currentTimeMillis()  // Tiempo al terminar
//                        val duration = endTime - startTime
//                        Log.d("TIEMPO_CARGA", "Carga completada en $duration ms")
//                        if (lista.isNotEmpty()) {
//                            handler.postDelayed({
//                                binding.recicleDispositivos.isVisible = true
//                                binding.LinealCargaDispo.isVisible = false
//                                binding.swipe.isVisible = true
//                            }, duration)
//
////                            inicializarRecicle(lista_vincualdos)
//                        } else {
//
//                            binding.noDispositivo.isVisible = true
//                            binding.recicleDispositivos.isVisible = false
//                            binding.LinealCargaDispo.isVisible = false
//                            binding.swipe.isVisible = false
//                        }
//
//
//                    }
//                }
//
//                "usuario" -> {
//                    val db = FirebaseFirestore.getInstance()
//                        .collection("Trabajadores_Usuarios_Drivers")
//                        .document("usuarios").collection("usuarios")
//                        .document(firebaseAuth.uid.toString()).collection("vinculados")
//
//                    db.get().addOnSuccessListener { res ->
//                        val spannableString =
//                            SpannableString("Disposivos vinculados : ${res.size().toString()}")
//                        constantestextos_general.setearInformacionboldDescripcion(
//                            "Disposivos vinculados",
//                            spannableString, binding.TotalDispo
//                        )
//                        lista.clear()
//                        for (datos in res) {
//                            val data = datos.data
//                            val dispositivo = data?.get("dispositivo") as? String ?: ""
//                            val fecha_registro = data?.get("fecha_registro") as? String ?: ""
//                            val hora_registro = data?.get("hora_registro") as? String ?: ""
//                            val id_dispositivo = data?.get("id_dispositivo") as? String ?: ""
//                            val ultima_con = data?.get("ultima_con") as? String ?: ""
//                            val untima_fecha_con = data?.get("untima_fecha_con") as? String ?: ""
//                            val primario = data?.get("primario") as? Boolean ?: false
//                            val marca = obtenerMarcaDesdeModelo(dispositivo)
//                            val datos = dataclass_dispo_vinculados(
//                                id_dispositivo,
//                                dispositivo,
//                                hora_registro,
//                                fecha_registro, marca, primario, untima_fecha_con, ultima_con
//                            )
//                            lista.add(datos)
//                        }
//
//                        val endTime = System.currentTimeMillis()  // Tiempo al terminar
//                        val duration = endTime - startTime
//                        Log.d("TIEMPO_CARGA", "Carga completada en $duration ms")
//                        if (lista.isNotEmpty()) {
//                            handler.postDelayed({
//                                binding.recicleDispositivos.isVisible = true
//                                binding.LinealCargaDispo.isVisible = false
//                                binding.swipe.isVisible = true
//                            }, duration)
////                            inicializarRecicle(lista_vincualdos)
//                        } else {
//                            binding.noDispositivo.isVisible = true
//                            binding.recicleDispositivos.isVisible = false
//                            binding.LinealCargaDispo.isVisible = false
//                            binding.swipe.isVisible = false
//                        }
//                    }
//                }
//
//                else -> Log.d("RESULT", "No se encontró el usuario")
//            }
//        }
//    }

    private fun confSwipe() {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.swipe.isRefreshing = false
//                obtener_dispositivos_vinculados()
            }, 2000)
        }
    }


    private fun inicializarRecicle(
        lista_vincualdos: List<dataclass_dispo_vinculados?>
    ) {
        val recicle = binding.recicleDispositivos
        recicle.layoutManager = LinearLayoutManager(this)
        recicle.adapter =
            adapter_dispo_vinculados(lista_vincualdos as MutableList<dataclass_dispo_vinculados>) { idDispoSeleccionado ->
                val androidId = obtenerAndroidID(this)
                vmUsuariosGenerales?.encontrar_user?.observe(this@activity_dispositivos_vinculados) { acceso ->
                    val (encontrado, coleccion) = acceso
//                    if (encontrado && coleccion != null) {
//                        vmDispoVinculados?.puedeCerrarSesionDispositivo(
//                            coleccion,
//                            idDispoSeleccionado.id_dispo.toString(),
//                            androidId, idDispoSeleccionado.nombre_dispo.toString()
//                        )
//
////                        referencia_vinculado.get().addOnSuccessListener { resActual ->
////                            if (resActual.exists()) {
////                                val dataActual = resActual.data
////                                val esPrimarioActual =
////                                    dataActual?.get("primario") as? Boolean ?: false
////                                val vinculados = coleccion.document(firebaseAuth.uid.toString())
////                                    .collection("vinculados")
////
////                                vinculados.get().addOnSuccessListener { documentos ->
////                                    val existePrimario =
////                                        documentos.any { it.getBoolean("primario") == true }
////                                    if (id.id_dispo == androidId) {
////                                        if (existePrimario) {
////                                            Toast.makeText(
////                                                this,
////                                                "No puedes cerrar sesión directa en este dispositivo",
////                                                Toast.LENGTH_SHORT
////                                            ).show()
////                                        } else {
////                                            // No hay un primario aún → ofrecer opción para establecer como primario
////                                            dialog = BottomSheetDialog(this)
////                                            dialog_cerrar_seccion(
////                                                id.nombre_dispo.toString(),
////                                                id.id_dispo.toString(),
////                                                false
////                                            )
////                                            dialog.show()
////                                        }
////                                    } else {
////                                        // Está intentando cerrar sesión en otro dispositivo
////                                        if (existePrimario) {
////                                            if (esPrimarioActual) {
////                                                // Soy el primario, puedo cerrar sesión a otros
////                                                dialog = BottomSheetDialog(this)
////                                                dialog_cerrar_seccion(
////                                                    id.nombre_dispo.toString(),
////                                                    id.id_dispo.toString(),
////                                                    true
////                                                )
////                                                dialog.show()
////                                            } else {
////                                                // No soy el primario, no puedo cerrar sesión a nadie
////                                                Toast.makeText(
////                                                    this,
////                                                    "Solo el dispositivo primario puede cerrar sesión en otros dispositivos",
////                                                    Toast.LENGTH_SHORT
////                                                ).show()
////                                            }
////                                        } else {
////                                            // No hay primario aún → permitir cerrar sesión
////                                            dialog = BottomSheetDialog(this)
////                                            dialog_cerrar_seccion(
////                                                id.nombre_dispo.toString(),
////                                                id.id_dispo.toString(),
////                                                false
////                                            )
////                                            dialog.show()
////                                        }
////                                    }
////                                }
////                            }
////                        }
//                    }

                }
//                encontrarUser(firebaseAuth.uid.toString()) { tipo, coleccion ->
//                    val docRefActual = when (tipo) {
//                        "trabajador" -> FirebaseFirestore.getInstance()
//                            .collection("Trabajadores_Usuarios_Drivers")
//                            .document("trabajadores").collection("trabajadores")
//                            .document(firebaseAuth.uid.toString())
//                            .collection("vinculados")
//                            .document(androidId)  // Verificamos el dispositivo actual
//
//                        "usuario" -> FirebaseFirestore.getInstance()
//                            .collection("Trabajadores_Usuarios_Drivers")
//                            .document("usuarios").collection("usuarios")
//                            .document(firebaseAuth.uid.toString())
//                            .collection("vinculados")
//                            .document(androidId)
//
//                        else -> {
//                            Log.d("RESULT", "No se encontró el usuario")
//                            return@encontrarUser
//                        }
//                    }
//
//                    docRefActual.get().addOnSuccessListener { resActual ->
//                        if (resActual.exists()) {
//                            val dataActual = resActual.data
//                            val esPrimarioActual = dataActual?.get("primario") as? Boolean ?: false
//
//                            val vinculadosRef = when (tipo) {
//                                "trabajador" -> FirebaseFirestore.getInstance()
//                                    .collection("Trabajadores_Usuarios_Drivers")
//                                    .document("trabajadores").collection("trabajadores")
//                                    .document(firebaseAuth.uid.toString()).collection("vinculados")
//
//                                "usuario" -> FirebaseFirestore.getInstance()
//                                    .collection("Trabajadores_Usuarios_Drivers")
//                                    .document("usuarios").collection("usuarios")
//                                    .document(firebaseAuth.uid.toString()).collection("vinculados")
//
//                                else -> return@addOnSuccessListener
//                            }
//
//                            vinculadosRef.get().addOnSuccessListener { documentos ->
//                                val existePrimario =
//                                    documentos.any { it.getBoolean("primario") == true }
//
//                                if (id.id_dispo == androidId) {
//                                    if (existePrimario) {
//                                        // Si ya hay un primario, y está intentando cerrar sesión en sí mismo (prohibido)
//                                        Toast.makeText(
//                                            this,
//                                            "No puedes cerrar sesión directa en este dispositivo",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    } else {
//                                        // No hay un primario aún → ofrecer opción para establecer como primario
//                                        dialog = BottomSheetDialog(this)
//                                        dialog_cerrar_seccion(
//                                            id.nombre_dispo.toString(),
//                                            id.id_dispo.toString(),
//                                            false
//                                        )
//                                        dialog.show()
//                                    }
//                                } else {
//                                    // Está intentando cerrar sesión en otro dispositivo
//                                    if (existePrimario) {
//                                        if (esPrimarioActual) {
//                                            // Soy el primario, puedo cerrar sesión a otros
//                                            dialog = BottomSheetDialog(this)
//                                            dialog_cerrar_seccion(
//                                                id.nombre_dispo.toString(),
//                                                id.id_dispo.toString(),
//                                                true
//                                            )
//                                            dialog.show()
//                                        } else {
//                                            // No soy el primario, no puedo cerrar sesión a nadie
//                                            Toast.makeText(
//                                                this,
//                                                "Solo el dispositivo primario puede cerrar sesión en otros dispositivos",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    } else {
//                                        // No hay primario aún → permitir cerrar sesión
//                                        dialog = BottomSheetDialog(this)
//                                        dialog_cerrar_seccion(
//                                            id.nombre_dispo.toString(),
//                                            id.id_dispo.toString(),
//                                            false
//                                        )
//                                        dialog.show()
//                                    }
//                                }
//
//                            }.addOnFailureListener {
//                                Toast.makeText(
//                                    this,
//                                    "Error al verificar dispositivos vinculados",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        } else {
//                            Toast.makeText(
//                                this,
//                                "No se encontró información del dispositivo actual",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }.addOnFailureListener {
//                        Toast.makeText(
//                            this,
//                            "Error al consultar el dispositivo actual",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//
//                }


            }
    }

    private fun dialog_cerrar_seccion(nombre_dispo: String, id_dispo: String, sinExsitir: Boolean) {
        val bottomSheetBinding =
            BottomSheetCerraSeccionConfirBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheetBinding.root
        val androidId = obtenerAndroidID(this)

        dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.show()

        if (!sinExsitir) {
            bottomSheetBinding.camposprimario.isVisible = true
            bottomSheetBinding.primario.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    dialog.dismiss()
                    encontrarUser(firebaseAuth.uid.toString()) { tipo, _ ->
                        val docRef = when (tipo) {
                            "trabajador" -> FirebaseFirestore.getInstance()
                                .collection("Trabajadores_Usuarios_Drivers")
                                .document("trabajadores").collection("trabajadores")
                                .document(firebaseAuth.uid.toString())
                                .collection("vinculados")
                                .document(id_dispo)

                            "usuario" -> FirebaseFirestore.getInstance()
                                .collection("Trabajadores_Usuarios_Drivers")
                                .document("usuarios").collection("usuarios")
                                .document(firebaseAuth.uid.toString())
                                .collection("vinculados")
                                .document(id_dispo)

                            else -> {
                                Log.d("RESULT", "No se encontró el usuario")
                                return@encontrarUser
                            }
                        }
                        Log.d("vemos_igualdad", "$id_dispo $androidId")
                        val hasmap = hashMapOf<String, Any>("primario" to true)
                        docRef.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Dispositivo primario guardado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
//                            obtener_dispositivos_vinculados()


                        }.addOnFailureListener { e ->
                            Log.d("error_dispo", "Error al colocar primario el dispositivo: $e")
                        }
                    }
                }
            }

        } else {

            bottomSheetBinding.camposprimario.isVisible = true

            bottomSheetBinding.primario.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    dialog.dismiss()
                    encontrarUser(firebaseAuth.uid.toString()) { tipo, _ ->
                        val docRef = when (tipo) {
                            "trabajador" -> FirebaseFirestore.getInstance()
                                .collection("Trabajadores_Usuarios_Drivers")
                                .document("trabajadores").collection("trabajadores")
                                .document(firebaseAuth.uid.toString())
                                .collection("vinculados")
                                .document(id_dispo)

                            "usuario" -> FirebaseFirestore.getInstance()
                                .collection("Trabajadores_Usuarios_Drivers")
                                .document("usuarios").collection("usuarios")
                                .document(firebaseAuth.uid.toString())
                                .collection("vinculados")
                                .document(id_dispo)

                            else -> {
                                Log.d("RESULT", "No se encontró el usuario")
                                return@encontrarUser
                            }
                        }

                        val hasmap = hashMapOf<String, Any>("primario" to true)
                        docRef.set(hasmap, SetOptions.merge()).addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Dispositivo primario guardado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
//                            obtener_dispositivos_vinculados()
                        }.addOnFailureListener { e ->
                            Log.d("error_dispo", "Error al colocar primario el dispositivo: $e")
                        }
                    }
                }
            }

            selecionar_dialog_biometricocuenta(nombre_dispo, id_dispo)
        }
    }

    private fun selecionar_dialog_biometricocuenta(nombre_dispo: String, id_disp: String) {
        val botto_shhet_biometrico_cuenta_user = BottomSheetlCrrsVinculadosBinding.inflate(
            LayoutInflater.from(this)
        )

        val view_biometrico = botto_shhet_biometrico_cuenta_user.root
        val biometrico = botto_shhet_biometrico_cuenta_user.biometrico
        val cuenta_normal = botto_shhet_biometrico_cuenta_user.seccionNormal
        biometrico.setOnClickListener {
            cerrar_seccion_biometrico(nombre_dispo, id_disp)
        }

        cuenta_normal.setOnClickListener {
            dialog_cerra_seccion_cuenta_user(id_disp)
        }

        dialog.setContentView(view_biometrico)
        dialog.show()
    }

    private fun cerrar_seccion_biometrico(nombre_dispo: String, id_dispo: String) {
        val biometricManager = BiometricManager.from(this)
        val biometricAuth =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val firebaseAuth = FirebaseAuth.getInstance()

        when (biometricManager.canAuthenticate(biometricAuth)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)

                val biometricPrompt = BiometricPrompt(
                    this,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            Toast.makeText(
                                applicationContext,
                                "Autenticación exitosa",
                                Toast.LENGTH_SHORT
                            ).show()

                            encontrarUser(firebaseAuth.uid.toString()) { tipo, _ ->
                                val docRef = when (tipo) {
                                    "trabajador" -> FirebaseFirestore.getInstance()
                                        .collection("Trabajadores_Usuarios_Drivers")
                                        .document("trabajadores").collection("trabajadores")
                                        .document(firebaseAuth.uid.toString())
                                        .collection("vinculados")
                                        .document(id_dispo)

                                    "usuario" -> FirebaseFirestore.getInstance()
                                        .collection("Trabajadores_Usuarios_Drivers")
                                        .document("usuarios").collection("usuarios")
                                        .document(firebaseAuth.uid.toString())
                                        .collection("vinculados")
                                        .document(id_dispo)

                                    else -> {
                                        Log.d("RESULT", "No se encontró el usuario")
                                        return@encontrarUser
                                    }
                                }
                                docRef.get().addOnSuccessListener { res ->
                                    if (res.exists()) {
                                        val data = res.data
                                        val dispositivo = data?.get("dispositivo") as? String ?: ""
                                        docRef.delete()
                                            .addOnSuccessListener {
                                                vmNotificaciones?.notificar_cerrado_seccion_vinculado(
                                                    firebaseAuth.uid.toString(),
                                                    dispositivo
                                                )
                                                dialog.dismiss()
                                                vmNotificaciones?._notifica_cerrado_seccion?.observe(
                                                    this@activity_dispositivos_vinculados
                                                ) { dispositivos ->
                                                    dispositivos?.forEach { it ->
                                                        if (it.nombre_dispo != null) {
                                                            val validacion =
                                                                vmTextosValidaciones?.modelos_celulares_iguales_directo(
                                                                    it.nombre_dispo,
                                                                    dispositivo
                                                                ) ?: false
                                                            if (validacion) {
                                                                lifecycleScope.launch {
                                                                    val notificacionRS =
                                                                        NotificacionRS()
                                                                    notificacionRS.enviarNotificacionFCM(
                                                                        it.tok_dispo.toString(),
                                                                        "idAdmin",
                                                                        "idasdasda",
                                                                        "iadasdasda",
                                                                        "entrada",
                                                                        "El administrador cerró tu sesión",
                                                                        "Tu sesión en el dispositivo $dispositivo fue cerrada por el administrador. Si no reconoces esta acción, te recomendamos contactarte con geinz work "
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }


                                            }

                                            .addOnFailureListener { e ->
                                                Log.d(
                                                    "error_eliminar",
                                                    "Error al eliminar el dispositivo: ${e.message}"
                                                )
                                            }
                                    }

                                }

                            }
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            super.onAuthenticationError(errorCode, errString)
                            Toast.makeText(
                                applicationContext,
                                "Error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            Toast.makeText(
                                applicationContext,
                                "Falló la autenticación",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verifica tu identidad")
                    .setSubtitle("Usa tu rostro, huella o clave para cerrar sesión en \"$nombre_dispo\"")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(
                    this,
                    "Tu dispositivo no tiene sensor biométrico",
                    Toast.LENGTH_SHORT
                ).show()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "El sensor biométrico no está disponible", Toast.LENGTH_SHORT)
                    .show()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    this,
                    "No has registrado ninguna huella. Te llevaremos a configurarla.",
                    Toast.LENGTH_LONG
                ).show()

                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        biometricAuth
                    )
                }
                startActivity(enrollIntent)
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Toast.makeText(
                    this,
                    "Se requiere una actualización de seguridad para usar la biometría.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Toast.makeText(
                    this,
                    "La biometría no está disponible en tu dispositivo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun dialog_cerra_seccion_cuenta_user(id_dispo: String) {
        val bottomSheet_verificar =
            BottomSheetUserCrrsVincualdosBinding.inflate(LayoutInflater.from(this))
        val view = bottomSheet_verificar.root
        val androidId = obtenerAndroidID(this)
        bottomSheet_verificar.BtnIngresar.setOnClickListener {
            val gmail = bottomSheet_verificar.ingreseSuMail.text.toString().trim()
            val contra = bottomSheet_verificar.txtpassword.text.toString().trim()

            if (gmail.isEmpty() || contra.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(gmail, contra)
                    .addOnSuccessListener { resultado ->
                        val user = resultado.user
                        val userId = user?.uid

                        if (firebaseAuth.uid.toString() == userId) {
                            bottomSheet_verificar.cerrandoSeccion.isVisible = true
                            bottomSheet_verificar.camposCerrarSeccion.isVisible = false

                            encontrarUser(firebaseAuth.uid.toString()) { tipo, _ ->
                                val docRef = when (tipo) {
                                    "trabajador" -> FirebaseFirestore.getInstance()
                                        .collection("Trabajadores_Usuarios_Drivers")
                                        .document("trabajadores").collection("trabajadores")
                                        .document(firebaseAuth.uid.toString())
                                        .collection("vinculados")
                                        .document(id_dispo)

                                    "usuario" -> FirebaseFirestore.getInstance()
                                        .collection("Trabajadores_Usuarios_Drivers")
                                        .document("usuarios").collection("usuarios")
                                        .document(firebaseAuth.uid.toString())
                                        .collection("vinculados")
                                        .document(id_dispo)

                                    else -> {
                                        Log.d("RESULT", "No se encontró el usuario")
                                        return@encontrarUser
                                    }
                                }

                                docRef.delete()
                                    .addOnSuccessListener {
                                        bottomSheet_verificar.cerrandoSeccion.isVisible = false
                                        dialog.dismiss()
//                                        obtener_dispositivos_vinculados()
                                        Log.d(
                                            "dispo_vinculado",
                                            "Dispositivo eliminado correctamente"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.d(
                                            "error_eliminar",
                                            "Error al eliminar el dispositivo: ${e.message}"
                                        )
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        val errorMessage = when {
                            e.message?.contains("The email address is badly formatted") == true ->
                                "El correo ingresado no es válido"

                            e.message?.contains("There is no user record") == true ->
                                "No se encontró una cuenta con ese correo"

                            e.message?.contains("The password is invalid") == true ->
                                "La contraseña es incorrecta"

                            else ->
                                "Error al iniciar sesión: ${e.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }


}