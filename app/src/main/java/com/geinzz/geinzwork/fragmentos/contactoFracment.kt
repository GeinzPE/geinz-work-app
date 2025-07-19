package com.geinzz.geinzwork.fragmentos

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.utils.constantes.constantes.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantes
import com.geinzz.geinzwork.databinding.FragmentContactoFracmentBinding
import com.geinzz.geinzwork.utils.constantes.constantes.constantesNoticias
import com.geinzz.geinzwork.utils.constantes.constantes.filtradoLocalidadElementos
import com.geinzz.geinzwork.databinding.ItemCustomCricleNoticiasTiendaBinding
import com.geinzz.geinzwork.model.dataClassAnuncios
import com.geinzz.geinzwork.vistaTiendas.NoticiasYofertasFiltrado
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class contactoFracment : Fragment() {

    private lateinit var binding: FragmentContactoFracmentBinding
    private lateinit var mcontex: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private lateinit var showBottomSheetButton: ImageView

    var localidadActual: String = ""
    var categoriaActual: String = ""

    private val kEY_CATEGOIRA = "MI_KEY"
    private val kEY_LOCALIDA = "MI_KEY2"

    val lista = mutableListOf<CarouselItem>()


    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentContactoFracmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val keyLocalidad = pref?.getString(kEY_LOCALIDA, "Default Value").toString()
        val keyCategoria = pref?.getString(kEY_CATEGOIRA, "Default Value").toString()
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        obtenerTiendas_noticias()
        var zoonIn = AnimationUtils.loadAnimation(mcontex, com.geinzz.geinzwork.R.anim.zoom_in)
        val zoomout = AnimationUtils.loadAnimation(mcontex, com.geinzz.geinzwork.R.anim.zoom_uot)
        showBottomSheetButton = binding.filtrado
        dialog = BottomSheetDialog(mcontex)
        binding.swipe.isVisible = true
        showBottomSheetButton.setOnClickListener {
            constantesNoticias.filtrado_bottom(binding.encontrados,
                binding.recielAnuncios,
                binding.loading,
                binding.linealappLayout,
                binding.filtrado,
                zoomout,
                zoonIn,
                binding.relativeNoEncontrado,
                binding.filtradoUsuairo.text.toString(),
                binding.filtradoCateogoriaPromo.text.toString(),
                mcontex,
                dialog,
                binding.filtradoGeneral,
                binding.filtradoUsuairo,
                binding.filtradoCateogoriaPromo,
                { localidad ->
                    binding.filtradoUsuairo.text = localidad
                },
                { categoria ->
                    binding.filtradoCateogoriaPromo.text = categoria
                }
            )
            dialog.show()

        }

        binding.noResultados.setOnClickListener {
            constantesNoticias.filtrado_bottom(binding.encontrados,
                binding.recielAnuncios,
                binding.loading,
                binding.linealappLayout,
                binding.filtrado,
                zoomout,
                zoonIn,
                binding.relativeNoEncontrado,
                binding.filtradoUsuairo.text.toString(),
                binding.filtradoCateogoriaPromo.text.toString(),
                mcontex,
                dialog,
                binding.filtradoGeneral,
                binding.filtradoUsuairo,
                binding.filtradoCateogoriaPromo,
                { localidad ->
                    binding.filtradoUsuairo.text = localidad
                },
                { categoria ->
                    binding.filtradoCateogoriaPromo.text = categoria
                }
            )
            dialog.show()

        }

        if (firebaseAuth.currentUser == null) {
            binding.filtradoCateogoriaPromo.text = Variables.General
        } else if (keyLocalidad.isNullOrEmpty() || keyLocalidad.equals("Default Value")) {
            filtradoLocalidadElementos.obtenerLocalidadUser { localida ->
                binding.filtradoUsuairo.text = localida
            }
            constantes.carga(
                3000,
                { obtenerNoticiasGeneral(zoomout, zoonIn) })

        } else {
            binding.filtradoUsuairo.text = keyLocalidad
            constantes.carga(
                3000,
                { obtenerFitlradoNoticias(keyCategoria, keyLocalidad, zoomout, zoonIn) })
        }

        if (keyCategoria.isNullOrEmpty() || keyCategoria.equals("Default Value")) {
            binding.filtradoCateogoriaPromo.text = Variables.General
            constantes.carga(
                3000,
                { obtenerNoticiasGeneral(zoomout, zoonIn) })
        } else {
            binding.filtradoCateogoriaPromo.text = keyCategoria
            constantes.carga(
                3000,
                { obtenerFitlradoNoticias(keyCategoria, keyLocalidad, zoomout, zoonIn) })
        }


        // Listener para filtradoUsuario
        binding.filtradoUsuairo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                localidadActual = s.toString()
                llamarObtnerFiltradoSiLocalidadEsReconocida(
                    localidadActual,
                    categoriaActual,
                    zoomout,
                    zoonIn
                )
                guardarShader(kEY_LOCALIDA, pref, localidadActual)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.filtradoCateogoriaPromo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                categoriaActual = s.toString()
                llamarObtnerFiltradoSiLocalidadEsReconocida(
                    localidadActual,
                    categoriaActual,
                    zoomout,
                    zoonIn
                )
                guardarShader(kEY_CATEGOIRA, pref, categoriaActual)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        confSwipe(keyLocalidad, keyCategoria, zoonIn, zoomout)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confSwipe(
        keyLocalidad: String,
        keyCategoria: String,
        zoonIn: Animation,
        zoomout: Animation
    ) {
        binding.swipe.setOnRefreshListener {
            binding.swipe.setColorSchemeResources(R.color.violeta)
            binding.swipe.isVisible = true

            val esUsuarioAutenticado = firebaseAuth.currentUser != null
            val esLocalidadValida = !keyLocalidad.isNullOrEmpty() && keyLocalidad != "Default Value"
            val esCategoriaValida = !keyCategoria.isNullOrEmpty() && keyCategoria != "Default Value"

            if (!esUsuarioAutenticado) {
                // 🔹 No hay usuario autenticado → Cargar noticias generales
                constantes.carga(1000) {
                    obtenerNoticiasGeneral(zoomout, zoonIn)
                    binding.swipe.isRefreshing = false // Detener swipe después de cargar
                }
            } else if (esLocalidadValida && esCategoriaValida) {
                // 🔹 Si hay usuario autenticado y localidad/categoría válidas → Cargar noticias filtradas
                constantes.carga(1000) {
                    obtenerFitlradoNoticias(keyCategoria, keyLocalidad, zoomout, zoonIn)
                    binding.swipe.isRefreshing = false // Detener swipe después de cargar
                }
            } else {
                // 🔹 Si hay usuario autenticado pero no localidad/categoría → Mostrar noticias generales
                constantes.carga(1000) {
                    obtenerNoticiasGeneral(zoomout, zoonIn)
                    binding.swipe.isRefreshing = false // Detener swipe después de cargar
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun llamarObtnerFiltradoSiLocalidadEsReconocida(
        localidad: String,
        categoria: String,
        zoomout: Animation?,
        zoonIn: Animation?,
    ) {
        when (localidad) {
            Variables.Supe, Variables.Barranca, Variables.Paramonga, Variables.Pativilca, Variables.General -> {
                obtenerFitlradoNoticias(categoria, localidad, zoomout, zoonIn)
            }

            else -> {
                println("Localidad no reconocida")
            }
        }
    }

    private fun guardarShader(KEY: String, pref: SharedPreferences?, valor: String) {
        val editor = pref?.edit()
        editor?.putString(KEY, valor)
        editor?.apply()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerFitlradoNoticias(
        categoria: String,
        localidad: String,
        zoomout: Animation?,
        zoonIn: Animation?,
    ) {
        val listaFiltrado = mutableListOf<dataClassAnuncios>()
        constantesNoticias.obtenerFiltradoNoticias(
            zoonIn,
            zoomout,
            listaFiltrado,
            categoria,
            binding.recielAnuncios,
            mcontex,
            localidad,
            binding.loading,
            binding.linealappLayout,
            binding.filtrado,
            binding.encontrados,
            binding.relativeNoEncontrado
        )
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun obtenerNoticiasGeneral(zoomout: Animation?, zoonIn: Animation?) {
        constantesNoticias.obtenerNoticiasGeneral(
            mcontex, binding.encontrados,
            binding.recielAnuncios,
            binding.loading,
            binding.linealappLayout, binding.filtrado, zoomout, zoonIn, binding.relativeNoEncontrado
        )
    }

    private fun obtenerTiendas_noticias() {
        val db = FirebaseFirestore.getInstance().collection(Variables.collection_Tiendas)
        db.get().addOnSuccessListener { res ->
            for (datos in res) {
                val data = datos.data
                val imgPerfil = data?.get(Variables.imgPerfil) as? String ?: ""
                val carucelItem = CarouselItem(imgPerfil)
                lista.add(carucelItem)
            }

            binding.carrucelTiendas.registerLifecycle(lifecycle)
            binding.carrucelTiendas.carouselListener = object : CarouselListener {
                override fun onCreateViewHolder(
                    layoutInflater: LayoutInflater,
                    parent: ViewGroup,
                ): ViewBinding? {
                    return ItemCustomCricleNoticiasTiendaBinding.inflate(
                        layoutInflater,
                        parent,
                        false
                    )
                }

                override fun onBindViewHolder(
                    binding: ViewBinding,
                    item: CarouselItem,
                    position: Int,
                ) {
                    val currentBinding = binding as ItemCustomCricleNoticiasTiendaBinding
                    val currentData = res.documents[position].data
                    val NombreTienda = currentData?.get(Variables.nombre) as? String ?: ""
                    val id = currentData?.get(Variables.id) as? String ?: ""
                    val imgPerfilTienda = currentData?.get(Variables.imgPerfil) as? String ?: ""

                    currentBinding.NombreTienda.text = NombreTienda
                    Glide.with(mcontex)
                        .load(imgPerfilTienda)
                        .into(currentBinding.img)
                    currentBinding.listener.setOnClickListener {
                        val vista = Intent(mcontex, NoticiasYofertasFiltrado::class.java).apply {
                            putExtra(Variables.idTienda, id)
                        }
                        startActivity(vista)
                    }
                }
            }
            binding.carrucelTiendas.setData(lista)
        }
    }


}


