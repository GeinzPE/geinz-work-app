package com.geinzz.geinzwork.vistaTiendas

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantesTrabajaConNosotros
import com.geinzz.geinzwork.databinding.ActivityTiendaNoLanzadaSpaslBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView

class tienda_no_lanzada_spasl : AppCompatActivity() {
    private lateinit var dialog: BottomSheetDialog
    lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var binding:ActivityTiendaNoLanzadaSpaslBinding
    private val categoriasTiendas = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityTiendaNoLanzadaSpaslBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.mandarSolicitud.setOnClickListener {
            dialog = BottomSheetDialog(this)
            constantesTrabajaConNosotros.llenarCampos(dialog,categoriasTiendas,this)
            dialog.show()
        }
    }

}