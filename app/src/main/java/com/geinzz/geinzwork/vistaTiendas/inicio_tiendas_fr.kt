package com.geinzz.geinzwork.vistaTiendas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.geinzz.geinzwork.utils.constantes.constantes.constantesPublicidad
import com.geinzz.geinzwork.utils.constantes.constantes.filtradoLocalidadElementos
import com.geinzz.geinzwork.databinding.FragmentInicioTiendasFrBinding
import com.geinzz.geinzwork.model.dataclass_item_general_tienda
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class inicio_tiendas_fr : Fragment() {
    private lateinit var binding: FragmentInicioTiendasFrBinding
    private lateinit var mcontex: Context
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private val tiendas = mutableListOf<dataclass_item_general_tienda>()
    private val KEY = "My_KEY"

    companion object {
        fun newInstance(filtrado: String): inicio_tiendas_fr {
            val fragment = inicio_tiendas_fr()
            val args = Bundle()
            args.putString("filtrado", filtrado)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInicioTiendasFrBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth=FirebaseAuth.getInstance()
        val pref = PreferenceManager.getDefaultSharedPreferences(mcontex)

        val storedValue = pref?.getString(KEY, "Default Value")
        binding.carritoCompras.setOnClickListener {
            if(firebaseAuth.currentUser==null){
                dialog = BottomSheetDialog(mcontex)
                constantesPublicidad.CreacionCuentaBottom_shett(
                    mcontex,
                    dialog
                )
                dialog.show()
            }else{
                startActivity(Intent(mcontex, carrito_compras::class.java))
            }

        }
        val filtrado = arguments?.getString("filtrado").toString()

        if (storedValue.isNullOrEmpty() || storedValue.equals("Default Value")) {
            binding.filtradotxt.text = "$filtrado"
            constantesVistaTiendas.obtenerTiendas(filtrado, tiendas, mcontex, binding.reciclGeneral,binding.loading,binding.linealPadre,binding.filtradobtn){filtrado->
                binding.saludFilter.text=filtrado
                binding.comidaFilter.text=filtrado
                binding.servircioFilter.text=filtrado
                binding.bodegasfilter.text=filtrado
            }
            constantesVistaTiendas.obtenerTiendaCategorias(
                filtrado,
                mcontex,
                binding.RecicleSalud,
                binding.RecicleVeiculos,
                binding.RecicleBodegas,
                binding.RecicleComida,binding.loading,binding.linealPadre,binding.filtradobtn

            )
            binding.comidaFilter.isVisible=false
            binding.saludFilter.isVisible=false
            binding.servircioFilter.isVisible=false
            binding.bodegasfilter.isVisible=false
        } else {
            binding.filtradotxt.text = "$storedValue"
            constantesVistaTiendas.obtenerTiendas(storedValue, tiendas, mcontex, binding.reciclGeneral,binding.loading,binding.linealPadre,binding.filtradobtn){filtrado->
                binding.saludFilter.text=filtrado
                binding.comidaFilter.text=filtrado
                binding.servircioFilter.text=filtrado
                binding.bodegasfilter.text=filtrado
            }
            constantesVistaTiendas.obtenerTiendaCategorias(
                storedValue,
                mcontex,
                binding.RecicleSalud,
                binding.RecicleVeiculos,
                binding.RecicleBodegas,
                binding.RecicleComida,binding.loading,binding.linealPadre,binding.filtradobtn

            )
            binding.comidaFilter.text=storedValue
            binding.saludFilter.text=storedValue
            binding.servircioFilter.text=storedValue
            binding.bodegasfilter.text=storedValue
        }

        constantesPublicidad.obtenerAnunciosIniciosFragment(binding.publicidadPrimaria, mcontex,"anunciosTienda1")
        constantesPublicidad.obtenerAnunciosIniciosFragment(binding.publicidadSegundaria, mcontex,"AnunciosTienda2")
        constantesPublicidad.obtenerAnunciosIniciosFragment(binding.publicidadTercera, mcontex,"AnunciosTienda3")
        constantesPublicidad.obtenerAnunciosIniciosFragment(binding.publicidadCuarta, mcontex,"AnunciosTienda4")



        binding.vermasServicios.setOnClickListener {
            val vista=Intent(mcontex, Vista_mostrarTodos_Tiendas::class.java).apply {
                putExtra("categoriaPasada","servicios")
                putExtra("localidadInicio",binding.filtradotxt.text.toString())
            }
            startActivity(vista)
        }
        binding.verMasSalud.setOnClickListener {
            val vista=Intent(mcontex, Vista_mostrarTodos_Tiendas::class.java).apply {
                putExtra("categoriaPasada","salud y belleza")
                putExtra("localidadInicio",binding.filtradotxt.text.toString())
            }
            startActivity(vista)
        }
        binding.verMasComida.setOnClickListener {
            val vista=Intent(mcontex, Vista_mostrarTodos_Tiendas::class.java).apply {
                putExtra("categoriaPasada","comida")
                putExtra("localidadInicio",binding.filtradotxt.text.toString())
            }
            startActivity(vista)
        }
        binding.verMasBodegas.setOnClickListener {
            val vista=Intent(mcontex, Vista_mostrarTodos_Tiendas::class.java).apply {
                putExtra("categoriaPasada","bodegas")
                putExtra("localidadInicio",binding.filtradotxt.text.toString())
            }
            startActivity(vista)
        }
        binding.verMasGeneral.setOnClickListener {
            val vista=Intent(mcontex, Vista_mostrarTodos_Tiendas::class.java).apply {
                putExtra("categoriaPasada","general")
                putExtra("localidadInicio",binding.filtradotxt.text.toString())
            }
            startActivity(vista)
        }

        binding.filtradobtn.setOnClickListener {
            dialog = BottomSheetDialog(mcontex)
            filtradoLocalidadElementos.filtradoNacionalidades("Seleccione su filtrado de Tiendas",mcontex, dialog) { seleccion ->
                binding.filtradotxt.text = "$seleccion"
            }
            dialog.show()
        }

        binding.filtradotxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (s.toString()) {
                    "Supe" -> {
                        obtnerFiltrado("Supe")
                        guardarShaderPref(pref, "Supe")
                    }

                    "Barranca" -> {
                        obtnerFiltrado("Barranca")
                        guardarShaderPref(pref, "Barranca")

                    }

                    "Paramonga" -> {
                        obtnerFiltrado("Paramonga")
                        guardarShaderPref(pref, "Paramonga")
                    }

                    "Pativilca" -> {
                        obtnerFiltrado("Pativilca")
                        guardarShaderPref(pref, "Pativilca")
                    }

                    "General" -> {
                        obtnerFiltrado("General")
                        guardarShaderPref(pref, "General")
                    }

                    else -> {
                        println("Localidad no reconocida")
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun obtnerFiltrado(filtrado: String) {
        //tiendas general
        constantesVistaTiendas.obtenerTiendas(
            filtrado,
            tiendas,
            mcontex,
            binding.reciclGeneral,binding.loading,binding.linealPadre,binding.filtradobtn
        ){filtrado->
            binding.saludFilter.text=filtrado
            binding.comidaFilter.text=filtrado
            binding.servircioFilter.text=filtrado
            binding.bodegasfilter.text=filtrado
        }
        // Trabajadores generales por categoría
        constantesVistaTiendas.obtenerTiendaCategorias(
            filtrado,
            mcontex,
            binding.RecicleSalud,
            binding.RecicleVeiculos,
            binding.RecicleBodegas,
            binding.RecicleComida,binding.loading,binding.linealPadre,binding.filtradobtn
        )

    }
    private fun guardarShaderPref(pref: SharedPreferences?, valor:String){
        val editor = pref?.edit()
        editor?.putString(KEY, valor)
        editor?.apply()

    }

}