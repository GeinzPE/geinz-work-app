package com.geinzz.geinzwork.servicios_geinz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import com.geinzz.geinzwork.crea_tu_noticia
import com.geinzz.geinzwork.Crea_tu_publicidad
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.utils.constantes.constantes.constantesTrabajaConNosotros
import com.geinzz.geinzwork.databinding.FragmentInicioServiciosFragmentBinding
import com.geinzz.geinzwork.databinding.ItemCustomFixedSizeLayout2Binding
import com.geinzz.geinzwork.vistasPubliciadesGeinz.crear_cuenta_Tienda
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDragHandleView
import org.imaginativeworld.whynotimagecarousel.listener.CarouselListener
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem
import org.imaginativeworld.whynotimagecarousel.utils.setImage

class inicio_servicios_fragment : Fragment() {
    private lateinit var binding: FragmentInicioServiciosFragmentBinding
    private lateinit var mcontex: Context
    lateinit var bottomSheet: BottomSheetDragHandleView
    private lateinit var dialog: BottomSheetDialog
    private val categoriasTiendas = ArrayList<String>()
    override fun onAttach(context: Context) {
        mcontex = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentInicioServiciosFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.mandarSolicitud.setOnClickListener {
            dialog = BottomSheetDialog(mcontex)
            constantesTrabajaConNosotros.llenarCampos(dialog, categoriasTiendas, mcontex)
            dialog.show()
        }
        binding.carruselPublicidadBaner.registerLifecycle(lifecycle)

        val listaUrls = listOf(
            "https://firebasestorage.googleapis.com/v0/b/geinzwork.appspot.com/o/IMG_CategoriasGeneral%2Fservicios_geinz_img%2F1.webp?alt=media&token=3befb9bd-280d-434d-8f3e-0d61bd6631ef",
            "https://firebasestorage.googleapis.com/v0/b/geinzwork.appspot.com/o/IMG_CategoriasGeneral%2Fservicios_geinz_img%2F2.webp?alt=media&token=932ee634-40d5-40d0-97bc-7040f76ca61f",
            "https://firebasestorage.googleapis.com/v0/b/geinzwork.appspot.com/o/IMG_CategoriasGeneral%2Fservicios_geinz_img%2F3.webp?alt=media&token=6e62ce06-1133-48bb-8dff-dbf00ab635d8"
        )

        val listaCarouselItems = listaUrls.map { url ->
            CarouselItem(imageUrl = url)
        }

        binding.carruselPublicidadBaner.carouselListener = object : CarouselListener {
            override fun onCreateViewHolder(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
            ): ViewBinding? {
                return ItemCustomFixedSizeLayout2Binding.inflate(
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
                val currentBinding = binding as ItemCustomFixedSizeLayout2Binding
                currentBinding.imageView.apply {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImage(item, R.drawable.ic_wb_cloudy_with_padding)
                    setOnClickListener {
                        when (position) {
                            0 -> {
                                val intent = Intent(context, crear_cuenta_Tienda::class.java)
                                context.startActivity(intent)
                            }

                            1 -> {
                                startActivity(Intent(mcontex, Crea_tu_publicidad::class.java))
                            }

                            2 -> {
                                startActivity(
                                    Intent(
                                        mcontex,
                                        crea_tu_noticia::class.java
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        binding.carruselPublicidadBaner.setData(listaCarouselItems)



        super.onViewCreated(view, savedInstanceState)
    }
}