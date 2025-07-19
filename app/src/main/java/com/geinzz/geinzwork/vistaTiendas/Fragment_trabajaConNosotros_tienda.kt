package com.geinzz.geinzwork.vistaTiendas


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.geinzz.geinzwork.utils.constantes.constantes.constantesTrabajaConNosotros
import com.geinzz.geinzwork.databinding.FragmentTrabajaConNosotrosTiendaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView


class Fragment_trabajaConNosotros_tienda : Fragment() {
    private lateinit var binding: FragmentTrabajaConNosotrosTiendaBinding
    lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var dialog: BottomSheetDialog
    private val categoriasTiendas = ArrayList<String>()
    private lateinit var mcontex: Context
    override fun onAttach(context: Context) {
        mcontex=context
        super.onAttach(context)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentTrabajaConNosotrosTiendaBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mandarSolicitud.setOnClickListener {
            dialog = BottomSheetDialog(mcontex)
            constantesTrabajaConNosotros.llenarCampos(dialog,categoriasTiendas,mcontex)
            dialog.show()
        }
    }
}