package com.example.geinzwork.vistaTrabajador

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.geinzwork.adapterViewholder.adapterCategoriasPromocionesFiltrado
import com.example.geinzwork.adapterViewholder.adapterPRmociones
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constanterAutcompleteTexviewGeneral
import com.example.geinzwork.dataclass.dataclasCaterogirasFiltrado
import com.example.geinzwork.dataclass.dataclasEncontrados
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityVerPromocionesBinding
import com.geinzz.geinzwork.databinding.BottomSheetFiltrarResultadosPromoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class ver_promociones : AppCompatActivity() {
    private lateinit var binding: ActivityVerPromocionesBinding
    private val listaCategorias = mutableListOf<dataclasCaterogirasFiltrado>()
    private lateinit var adapter: adapterPRmociones
    private lateinit var adapterCategorias: adapterCategoriasPromocionesFiltrado
    private val listaanunciosEncontrados = mutableListOf<dataclasEncontrados>()
    private lateinit var dialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerPromocionesBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        obtener_TodasPromociones()
        obtenerSucategoriasFiltrado()
        binding.search.addTextChangedListener { textoBusqueda ->
            val resultadosFiltrados = listaanunciosEncontrados.filter { nota ->
                nota.nombreTienda!!.lowercase().contains(textoBusqueda.toString().lowercase())
            }
            adapter.actulizarLista(resultadosFiltrados)
            if (resultadosFiltrados.isEmpty()) {
                binding.textoNoEncontrado.isVisible = true
                binding.recicleView.isVisible = false
            } else {
                binding.textoNoEncontrado.isVisible = false
                binding.recicleView.isVisible = true
            }
        }

        binding.filtradoCategorias.setOnClickListener {
            dialog = BottomSheetDialog(this)
            mostrarDialogFiltrado(binding.categoriaSelcionada.text.toString(), dialog, this)
            dialog.show()
        }
    }

    private fun mostrarDialogFiltrado(
        categoria: String,
        dialog: BottomSheetDialog,
        context: Context
    ) {
        var bindingBottomShet: BottomSheetFiltrarResultadosPromoBinding
        bindingBottomShet =
            BottomSheetFiltrarResultadosPromoBinding.inflate(LayoutInflater.from(context))
        val view = bindingBottomShet.root
        bindingBottomShet.cerrar.setOnClickListener {
            dialog.dismiss()
        }
        bindingBottomShet.btnApply.setOnClickListener {
            filtraResultadoSubcategoria(categoria, bindingBottomShet.subcategoriED.text.toString())
            dialog.dismiss()
        }
        bindingBottomShet.anuncioCategoria.text = "Categoria selecionada : ${categoria}"
        constanterAutcompleteTexviewGeneral.obtenerSubcategoriasPublicidades(
            categoria,
            bindingBottomShet.subcategoriED
        )
        dialog.setContentView(view)
    }


    private fun obtener_TodasPromociones() {
        binding.prograsvar.isVisible = true
        binding.recicleView.isVisible = false
        listaanunciosEncontrados.clear()
        val listaDocumentos = listOf(
            Variables.anunciosSegundarios,
            Variables.anunciosTerceros,
            Variables.anunciosCuartos,
            Variables.anunciosQuintos,
            Variables.anunciosSextos
        )

        val listaDocumentosInternos = (1..20).map { "anuncio$it" }

        for (documentos in listaDocumentos) {
            val db =
                FirebaseFirestore.getInstance().collection(Variables.anuncios).document(documentos)

            for (documentoInterno in listaDocumentosInternos) {
                val docRef = db.collection(Variables.anuncios).document(documentoInterno)
                docRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val data = snapshot.data
                        val disponible = data?.get(Variables.disponibleAN) as? Boolean ?: false
                        if (!disponible) {
                            val nombreAnuncio = data?.get(Variables.caption) as? String ?: ""
                            val idPRopietario = data?.get(Variables.idPropietario) as? String ?: ""
                            val imgURl = data?.get(Variables.imagenUrl) as? String ?: ""
                            val nombreTienda = data?.get(Variables.nombreTienda) as? String ?: ""
                            val descuento_boolean =
                                data?.get(Variables.descuento_boolean) as? Boolean ?: false
                            val porcentajeDescuento =
                                data?.get(Variables.porcentajeDescuento) as? Number ?: 0
                            val id = data?.get(Variables.id) as? String ?: ""
                            val fechas = data?.get(Variables.fechas) as? Map<String, String>
                            val fechaVencimiento =
                                fechas?.get(Variables.fecha_vencimiento) ?: "Fecha no disponible"
                            val item = dataclasEncontrados(
                                imgURl,
                                idPRopietario,
                                nombreTienda,
                                fechaVencimiento,
                                documentos,
                                id, descuento_boolean, porcentajeDescuento
                            )
                            listaanunciosEncontrados.add(item)
                                adapter.actulizarLista(listaanunciosEncontrados.toList()) // Genera una copia inmutable
                                binding.prograsvar.isVisible = false
                                binding.recicleView.isVisible = true
                                binding.textoNoEncontrado.isVisible = false


                            Log.d(
                                "anunciosEncontrados",
                                "Anuncios encontrados: $nombreAnuncio, $documentos, $imgURl"
                            )
                        }
                    } else {
                        Log.d("Firestore", "Documento no encontrado: $documentoInterno")
                        binding.prograsvar.isVisible = false
                        binding.textoNoEncontrado.isVisible=true
                    }
                }.addOnFailureListener { exception ->
                    Log.e("Firestore", "Error obteniendo el documento $documentoInterno", exception)
                }
            }
        }
        inicializarRecicleNotas(listaanunciosEncontrados)
    }

    private fun obtenerSucategoriasFiltrado() {
        val db = FirebaseFirestore.getInstance().collection(Variables.subcategoriasPublicidad)

        listaCategorias.clear()
        listaCategorias.add(dataclasCaterogirasFiltrado("Todos"))

        db.get().addOnSuccessListener { res ->
            for (document in res) {
                val documentName = document.id
                println("Nombre del documento: $documentName")

                binding.containerCargnadoCat.isVisible=false
                binding.categroiasFiltrado.isVisible=true


                val item = dataclasCaterogirasFiltrado(documentName)
                listaCategorias.add(item)
            }
            inicializarCategoriaS()
        }.addOnFailureListener { exception ->
            binding.containerCargnadoCat.isVisible=true
            binding.categroiasFiltrado.isVisible=false
            println("Error al obtener documentos: ${exception.message}")
        }
    }


    private fun filtraResultadoSubcategoria(categoriaP: String, subcategoriaP: String) {
        // Limpiar la lista y mostrar el ProgressBar
        listaanunciosEncontrados.clear()
        binding.prograsvar.isVisible = true
        binding.recicleView.isVisible = false
        binding.textoNoEncontrado.isVisible =
            false // Asegúrate de ocultar el mensaje de "No encontrado" al iniciar

        val listaDocumentos = listOf(
            Variables.anunciosSegundarios,
            Variables.anunciosTerceros,
            Variables.anunciosCuartos,
            Variables.anunciosQuintos,
            Variables.anunciosSextos
        )
        val listaDocumentosInternos = (1..20).map { "${Variables.anuncio}$it" }
        val totalTareas = listaDocumentos.size * listaDocumentosInternos.size
        var tareasCompletadas = 0


        val listaTemporal = mutableListOf<dataclasEncontrados>()
        for (documentos in listaDocumentos) {
            val db =
                FirebaseFirestore.getInstance().collection(Variables.anuncios).document(documentos)
            for (documentoInterno in listaDocumentosInternos) {
                val docRef = db.collection(Variables.anuncios).document(documentoInterno)
                docRef.get().addOnSuccessListener { res ->
                    tareasCompletadas++
                    if (res.exists()) {
                        val data = res.data
                        val disponible = data?.get(Variables.disponibleAN) as? Boolean ?: false
                        val categoria = data?.get(Variables.categoria) as? String ?: ""
                        val subcategoria = data?.get(Variables.subcategoria) as? String ?: ""
                        if (!disponible && categoria == categoriaP && subcategoria == subcategoriaP) {
                            Log.d("verificaomos igualdadr","${disponible} $categoria  $categoriaP $subcategoria $subcategoriaP")
                            val idPropietario = data?.get(Variables.idPropietario) as? String ?: ""
                            val imgURl = data?.get(Variables.imagenUrl) as? String ?: ""
                            val nombreTienda = data?.get(Variables.nombreTienda) as? String ?: ""
                            val descuento_boolean =
                                data?.get(Variables.descuento_boolean) as? Boolean ?: false
                            val porcentajeDescuento =
                                data?.get(Variables.porcentajeDescuento) as? Number ?: 0
                            val fechas = data?.get(Variables.fechas) as? Map<String, String>
                            val id = data?.get(Variables.id) as? String ?: ""
                            val fechaVencimiento =
                                fechas?.get(Variables.fecha_vencimiento) ?: "Fecha no disponible"
                            val item = dataclasEncontrados(
                                imgURl,
                                idPropietario,
                                nombreTienda,
                                fechaVencimiento,
                                documentos,
                                id, descuento_boolean, porcentajeDescuento
                            )
                            listaTemporal.add(item)
                        }
                    } else {
                        Log.d("Firestore", "Documento no encontrado: $documentoInterno")
                    }

                    // Verificar si todas las tareas se han completado
                    verificarCargaCompletada(tareasCompletadas, totalTareas, listaTemporal)
                }.addOnFailureListener {
                    tareasCompletadas++
                    verificarCargaCompletada(tareasCompletadas, totalTareas, listaTemporal)
                }
            }

        }
    }

    private fun filtraResultado(filtrado: String) {
        // Limpiar la lista y mostrar el ProgressBar
        listaanunciosEncontrados.clear()
        binding.prograsvar.isVisible = true
        binding.recicleView.isVisible = false
        binding.textoNoEncontrado.isVisible = false // Ocultar el mensaje "No encontrado" al inicio

        val listaDocumentos = listOf(
            Variables.anunciosSegundarios,
            Variables.anunciosTerceros,
            Variables.anunciosCuartos,
            Variables.anunciosQuintos,
            Variables.anunciosSextos
        )
        val listaDocumentosInternos = (1..20).map { "${Variables.anuncio}$it" }
        val totalTareas = listaDocumentos.size * listaDocumentosInternos.size
        var tareasCompletadas = 0



        val listaTemporal = mutableListOf<dataclasEncontrados>()

        for (documentos in listaDocumentos) {
            println("los docuemtos son $listaDocumentosInternos $listaDocumentos")
            val db = FirebaseFirestore.getInstance().collection(Variables.anuncios).document(documentos)
            for (documentoInterno in listaDocumentosInternos) {
                val docRef = db.collection(Variables.anuncios).document(documentoInterno)
                docRef.get()
                    .addOnSuccessListener { res ->
                        tareasCompletadas++
                        if (res.exists()) {
                            val data = res.data
                            Log.d("DEBUG", "Datos obtenidos del documento: $data")
                            val disponible = data?.get(Variables.disponibleAN) as? Boolean ?: false
                            val categoria = data?.get(Variables.categoria) as? String ?: ""

                            val documentoId = docRef.id  // ID del documento interno
                            val grupoDocumento = documentos  // Nombre del grupo de anuncios (como 'anunciosSextos', 'anunciosQuintos', etc.)

                            // Imprime los valores de la condición de filtro con el nombre del grupo de documentos
                            Log.d(
                                "DEBUG",
                                "Grupo de Documento: $grupoDocumento, Documento: $documentoId, Disponible: $disponible, Datos: $data, Categoría: '$categoria', Filtrado: '$filtrado'"
                            )
                            if (!disponible && categoria.trim().lowercase() == filtrado.trim().lowercase()) {
                                Log.d("DEBUG", "Coincidencia encontrada")
                                val idPropietario = data?.get(Variables.idPropietario) as? String ?: ""
                                val imgURl = data?.get(Variables.imagenUrl) as? String ?: ""
                                val nombreTienda = data?.get(Variables.nombreTienda) as? String ?: ""
                                val fechas = data?.get(Variables.fechas) as? Map<String, String>
                                val descuento_boolean =
                                    data?.get(Variables.descuento_boolean) as? Boolean ?: false
                                val porcentajeDescuento =
                                    data?.get(Variables.porcentajeDescuento) as? Number ?: 0
                                val id = data?.get(Variables.id) as? String ?: ""
                                val fechaVencimiento =
                                    fechas?.get(Variables.fecha_vencimiento) ?: "Fecha no disponible"

                                val item = dataclasEncontrados(
                                    imgURl,
                                    idPropietario,
                                    nombreTienda,
                                    fechaVencimiento,
                                    documentos,
                                    id,
                                    descuento_boolean,
                                    porcentajeDescuento
                                )
                                Log.d("DEBUG", "Item añadido: $item")
                                listaTemporal.add(item)
                            } else {
                                Log.d("DEBUG", "El documento no cumple con la condición del filtro.")
                            }
                        } else {
                            Log.d("DEBUG", "Documento no encontrado: $documentoInterno")
                        }
                        verificarCargaCompletada(tareasCompletadas, totalTareas, listaTemporal)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error al obtener documento: ${e.message}", e)
                        tareasCompletadas++
                        verificarCargaCompletada(tareasCompletadas, totalTareas, listaTemporal)
                    }
            }
        }

        binding.search.addTextChangedListener { textoBusqueda ->
            val resultadosFiltrados = listaTemporal.filter { item ->
                item.nombreTienda!!.lowercase().contains(textoBusqueda.toString().lowercase())
            }
            adapter.actulizarLista(resultadosFiltrados)
            if (resultadosFiltrados.isEmpty()) {
                binding.textoNoEncontrado.isVisible = true
                binding.recicleView.isVisible = false
            } else {
                binding.textoNoEncontrado.isVisible = false
                binding.recicleView.isVisible = true
            }
        }
    }



    private fun verificarCargaCompletada(
        tareasCompletadas: Int,
        totalTareas: Int,
        listaTemporal: List<dataclasEncontrados>
    ) {
        if (tareasCompletadas == totalTareas) {
            binding.prograsvar.isVisible = false
            Log.d("datos_lista",listaTemporal.toString())
            if (listaTemporal.isEmpty()) {
                binding.textoNoEncontrado.isVisible = true
            } else {
                binding.recicleView.isVisible = true
                adapter.actulizarLista(listaTemporal)
            }
        }
    }


    private fun inicializarCategoriaS() {
        val recicle = binding.categroiasFiltrado
        adapterCategorias = adapterCategoriasPromocionesFiltrado(listaCategorias) { item ->
            if (item.nombreCategoria.equals(Variables.Todos)) {
                obtener_TodasPromociones()
                binding.filtradoCategorias.isVisible = false
                binding.textoNoEncontrado.isVisible = false
            } else {
                binding.search.setText("")
                binding.textoNoEncontrado.isVisible = false
                binding.categoriaSelcionada.text = item.nombreCategoria
                binding.filtradoCategorias.isVisible = true
                binding.recicleView.isVisible = false
                binding.prograsvar.isVisible = true
                filtraResultado(item.nombreCategoria.toString().toLowerCase())
            }


        }
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recicle.adapter = adapterCategorias
    }


    private fun inicializarRecicleNotas(lista: MutableList<dataclasEncontrados>) {
        val recicle = binding.recicleView
        adapter = adapterPRmociones(lista)
        recicle.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recicle.adapter = adapter
    }
}