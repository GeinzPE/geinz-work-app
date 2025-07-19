package com.geinzz.geinzwork.vistaTiendas.hisrotiralfr

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geinzz.geinzwork.R
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.Toast
import androidx.core.view.isVisible
import com.geinzz.geinzwork.utils.constantes.constantes.animacionesCambio_color_btnFiltrado_historial
import com.geinzz.geinzwork.utils.constantes.constantes.constantesCarrito
import com.geinzz.geinzwork.databinding.BottomSheetFiltradoComprasCarritoBinding
import com.geinzz.geinzwork.databinding.FragmentComprasfrBinding
import com.geinzz.geinzwork.model.dataclassHistorial
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import com.google.android.material.button.MaterialButton


class comprasfr : Fragment() {
    private lateinit var binding: FragmentComprasfrBinding
    private lateinit var mcontex: Context
    private val listaHistorial = (listOf<dataclassHistorial>())
    private lateinit var dialog: BottomSheetDialog
    private lateinit var bottomSheetDialog: BottomSheetFiltradoComprasCarritoBinding
    private lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var listaBtnFiltrado: List<MaterialButton>
    private var tienda: Boolean = false
    private var precioFiltrado: Boolean = false
    private var filtradoFecha: Boolean = false
    private var filtradoEstado: Boolean = false
    private var Estado: String? = ""
    private var nombresTienda: String? = null
    private var listenersAsignados = false
    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentComprasfrBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog = BottomSheetDialog(mcontex)
        constantesCarrito.obtenerComprasReservas(
            "todos",
            listaHistorial,
            binding.recieleCompras,
            mcontex, nombreTienda = { nombres ->
                binding.productos.setOnClickListener {
                    bottomSheetFiltradoCompras(nombres, "todos")
                    dialog.show()

                }
            }, totalEncontrado = { total ->
                binding.encontrados.text = "Encontrados :${total}"
            })
        initializeButtons()


        android.util.Log.d("obtnemos_la_tienda", "las tiendas $nombresTienda")
    }

    private fun initializeButtons() {
        listaBtnFiltrado = listOf(binding.carritoCompras, binding.compraPromo, binding.todos)


        animacionesCambio_color_btnFiltrado_historial.styleBtn(
            mcontex,
            listaBtnFiltrado,
            binding.todos
        )


        listaBtnFiltrado.forEach { btn ->
            btn.setOnClickListener {
                animacionesCambio_color_btnFiltrado_historial.styleBtn(
                    mcontex,
                    listaBtnFiltrado,
                    btn
                )
                showToastForButton(btn)
            }
        }
    }

    private fun showToastForButton(button: MaterialButton) {

        val tipoCompra = when (button) {
            binding.carritoCompras -> "carrito"
            binding.compraPromo -> "promociones"
            binding.todos -> "todos"
            else -> return
        }

        constantesCarrito.obtenerComprasReservas(
            tipoCompra,
            listaHistorial,
            binding.recieleCompras,
            mcontex,
            nombreTienda = { nombres ->

                binding.productos.setOnClickListener {
                    bottomSheetFiltradoCompras(nombres, tipoCompra)
                    dialog.show()
                }
            }, totalEncontrado = { total ->
                binding.encontrados.text = "Encontrados :${total}"
            })
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun bottomSheetFiltradoCompras(nombres: List<String>, tipo: String) {
        bottomSheetDialog =
            BottomSheetFiltradoComprasCarritoBinding.inflate(LayoutInflater.from(mcontex))
        val view = bottomSheetDialog.root
        bottomSheet = view.findViewById(R.id.cerrar)
        val TiendaSwtch = bottomSheetDialog.Tienda
        val linealTienda = bottomSheetDialog.linealTienda
        val AutoCompleteTextViewTienda = bottomSheetDialog.TiendaED
        val sekBar = bottomSheetDialog.seekBar
        val textViewValue = bottomSheetDialog.textViewValue
        val preciofiltradoLY = bottomSheetDialog.preciofiltrado
        val linealSakeBAr = bottomSheetDialog.linealSakeBAr
        val valoMAXED = bottomSheetDialog.valoMAXED
        val filtradoFechaLY = bottomSheetDialog.filtradoFecha
        val linealFechaLY = bottomSheetDialog.linealFecha
        val fechaED = bottomSheetDialog.fechaED
        val filtradoEstadoLY = bottomSheetDialog.filtradoEstado
        val estadosLinealLY = bottomSheetDialog.estadosLineal
        val RadioGrup = bottomSheetDialog.RadioGrup
        val buttonSetMax = bottomSheetDialog.buttonSetMax
        val BTNfiltraID = bottomSheetDialog.filtraID

        val adapter = ArrayAdapter(mcontex, android.R.layout.simple_dropdown_item_1line, nombres)
        AutoCompleteTextViewTienda.setAdapter(adapter)

        ActivarEstadoSwhct(TiendaSwtch, linealTienda) { valor ->
            tienda = valor
            if (!valor) {
                AutoCompleteTextViewTienda.setText("")
            }
        }

        ActivarEstadoSwhct(
            preciofiltradoLY,
            linealSakeBAr
        ) { valor ->
            precioFiltrado = valor
            sekBar.setProgress(0)
            textViewValue.text = ""
            valoMAXED.setText("")
        }

        ActivarEstadoSwhct(
            filtradoFechaLY,
            linealFechaLY
        ) { valor ->
            filtradoFecha = valor
            if (!valor) {
                fechaED.setText("")
            }
        }

        ActivarEstadoSwhct(
            filtradoEstadoLY,
            estadosLinealLY
        ) { valor ->
            filtradoEstado = valor
            if (!valor) {
                RadioGrup.clearCheck()
                Estado = ""
            }
        }

        RadioGrup.setOnCheckedChangeListener { group, checkedId ->
            Estado = when (checkedId) {
                R.id.EnTienda -> {
                    "tienda"
                }

                R.id.camino -> {
                    "camino"
                }

                R.id.entragado -> {
                    "entragado"
                }

                R.id.pendiente -> {
                    "pendiente"
                }

                else -> {
                    ""
                }
            }
        }

        sekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewValue.text = "$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        buttonSetMax.setOnClickListener {
            val maxText = valoMAXED.text.toString()

            if (maxText.isNotEmpty()) {
                val maxValue = maxText.toIntOrNull()

                if (maxValue != null && maxValue > 0) {
                    sekBar.max = maxValue
                } else {
                    Toast.makeText(
                        mcontex,
                        "Ingresa un número válido mayor a 0",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(mcontex, "Ingresa un valor en el EditText", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        BTNfiltraID.setOnClickListener {
            var filtrosValidos = true

            if (tienda && AutoCompleteTextViewTienda.text.toString().isNullOrEmpty()) {
                AutoCompleteTextViewTienda.error = "Seleccione una tienda a filtrar"
                filtrosValidos = false
            }

            if (precioFiltrado && textViewValue.text.toString() == "") {
                Toast.makeText(mcontex, "Seleccione un precio para filtrar", Toast.LENGTH_SHORT)
                    .show()
                filtrosValidos = false
            }

            if (filtradoFecha && fechaED.text.toString().isNullOrEmpty()) {
                fechaED.error = "Seleccione una fecha para filtrar"
                filtrosValidos = false
            }

            if (filtradoEstado && Estado.isNullOrEmpty()) {
                Toast.makeText(mcontex, "Seleccione un estado para filtrar", Toast.LENGTH_SHORT)
                    .show()
                filtrosValidos = false
            }

            if (filtrosValidos) {
                constantesCarrito.obtenerComprasReservasFiltrado(
                    tipo,
                    AutoCompleteTextViewTienda.text.toString(),
                    textViewValue.text.toString(),
                    fechaED.text.toString(),
                    Estado.toString(),
                    listaHistorial,
                    binding.recieleCompras,
                    mcontex
                ) { filtrado ->
                    binding.encontrados.text = "Encontrados :${filtrado}"
                }
            }
        }

        dialog.setContentView(view)
    }


    private fun ActivarEstadoSwhct(
        swtch: Switch,
        lineal: LinearLayout,
        estadoVar: (Boolean) -> Unit,
    ) {
        swtch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lineal.isVisible = true
                estadoVar(true)
            } else {
                lineal.isVisible = false
                estadoVar(false)
            }
        }
    }

}


