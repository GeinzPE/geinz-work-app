package com.geinzz.geinzwork.vistasPubliciadesGeinz

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.geinzwork.constantesGeneral.Variables
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.databinding.ActivityPublicidadGeinzBinding
import com.geinzz.geinzwork.constantesGeneral.constantesNoticias
import com.geinzz.geinzwork.constantesGeneral.filtradoLocalidadElementos
import com.geinzz.geinzwork.dataclass.dataClassAnuncios
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class noticiasGeinzpb : AppCompatActivity() {

    private lateinit var binding: ActivityPublicidadGeinzBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dialog: BottomSheetDialog
    private lateinit var showBottomSheetButton: ImageView
    var localidadActual: String = ""
    var categoriaActual: String = ""
    private val kEY_CATEGOIRA = "MI_KEY"
    private val kEY_LOCALIDA = "MI_KEY2"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicidadGeinzBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseAuth = FirebaseAuth.getInstance()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val keyLocalidad = pref?.getString(kEY_LOCALIDA, "Default Value").toString()
        val keyCategoria = pref?.getString(kEY_CATEGOIRA, "Default Value").toString()
        firebaseAuth = FirebaseAuth.getInstance()
        var zoonIn = AnimationUtils.loadAnimation(this, com.geinzz.geinzwork.R.anim.zoom_in)
        val zoomout = AnimationUtils.loadAnimation(this, com.geinzz.geinzwork.R.anim.zoom_uot)
        showBottomSheetButton = binding.filtrado
        dialog = BottomSheetDialog(this)
        showBottomSheetButton.setOnClickListener {
            constantesNoticias.filtrado_bottom(binding.encontrados, binding.recielAnuncios,
                binding.loading,
                binding.linealappLayout,
                binding.filtrado,
                zoomout,
                zoonIn,
                binding.relativeNoEncontrado,
                binding.filtradoUsuairo.text.toString(),
                binding.filtradoCateogoriaPromo.text.toString(),
                this,
                dialog,null,null,null,
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
            constantesNoticias.filtrado_bottom(binding.encontrados,binding.recielAnuncios,
                binding.loading,
                binding.linealappLayout,
                binding.filtrado,
                zoomout,
                zoonIn,
                binding.relativeNoEncontrado,
                binding.filtradoUsuairo.text.toString(),
                binding.filtradoCateogoriaPromo.text.toString(),
                this,
                dialog,null,null,null,
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
            binding.filtradoCateogoriaPromo.text = "General"
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
            binding.filtradoCateogoriaPromo.text = "General"
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

        // Listener para filtradoCateogoriaPromo
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

    }

    // Función para llamar a obtnerFiltrado si la localidad es reconocida
    @RequiresApi(Build.VERSION_CODES.O)
    private fun llamarObtnerFiltradoSiLocalidadEsReconocida(
        localidad: String,
        categoria: String,
        zoomout: Animation?,
        zoonIn: Animation?
    ) {
        when (localidad) {
            Variables.Supe,  Variables.Barranca,  Variables.Paramonga,  Variables.Pativilca,  Variables.General -> {
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
        zoonIn: Animation?
    ) {
        val listaFiltrado = mutableListOf<dataClassAnuncios>()
        constantesNoticias.obtenerFiltradoNoticias(
            zoonIn,
            zoomout,
            listaFiltrado,
            categoria,
            binding.recielAnuncios,
            this,
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
            this,binding.encontrados,
            binding.recielAnuncios,
            binding.loading,
            binding.linealappLayout, binding.filtrado, zoomout, zoonIn, binding.relativeNoEncontrado
        )
    }

}