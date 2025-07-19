package com.geinzz.geinzwork.utils.constantes.constantes

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.BottomSheetCrearCuentaTiendaBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

object constantesTrabajaConNosotros {


    @SuppressLint("MissingInflatedId")
    fun llenarCampos(dialog: BottomSheetDialog,categoriasTiendas:ArrayList<String>,context: Context) {
        var bindingBottomShet: BottomSheetCrearCuentaTiendaBinding
        bindingBottomShet=BottomSheetCrearCuentaTiendaBinding.inflate(LayoutInflater.from(context))
        val view=bindingBottomShet.root
        bindingBottomShet.cerrar.setOnClickListener {
            dialog.dismiss()
        }
        val btnApply = bindingBottomShet.btnApply
        val nombreUser = bindingBottomShet.nombreED
        val dniUser = bindingBottomShet.dniED
        val correoElectronico = bindingBottomShet.correoElectronico
        val telefono = bindingBottomShet.telefonoed
        val nombreTienda = bindingBottomShet.nombreTiendaED
        val TelefonoTienda = bindingBottomShet.TelefonoTiendaED
        val categoriaTienda = bindingBottomShet.categoria
        val subcategoriaTienda = bindingBottomShet.subcategoria

        val db = FirebaseFirestore.getInstance().collection("categorias").document("categoriasTiendas")

        db.get()
            .addOnSuccessListener { res ->
                if (res.exists()) {
                    val categorias = res.get("categorias") as? ArrayList<String>
                    categorias?.let {
                        categoriasTiendas.addAll(it)
                        val adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_dropdown_item_1line,
                            categorias
                        )
                        categoriaTienda.setAdapter(adapter)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error obteniendo documentos", exception)
            }
        constantesCarrito.setearDatosUsuario { nombre, numero, localida, apellido ->
            bindingBottomShet.nombreED.setText(nombre)
            bindingBottomShet.telefonoed.setText(numero)
        }

        categoriaTienda.setOnItemClickListener { parent, view, position, id ->
            val selectedCategory = parent.getItemAtPosition(position).toString()
            actualizarSubcategorias(context, selectedCategory, subcategoriaTienda)
        }

        val grupoRadios = bindingBottomShet.grupoVirtualFisica

        val radioLocalidad = bindingBottomShet.grupoLocalida

        var fisicaoVirtual = ""
        var localidad = ""



        radioLocalidad.setOnCheckedChangeListener { _, checkedId ->
            localidad = when (checkedId) {
                R.id.Barranca -> "Barranca"
                R.id.Supe -> "Supe"
                R.id.Paramonga -> "Paramonga"
                R.id.Pativilca -> "Pativilca"
                else -> ""
            }
        }



        grupoRadios.setOnCheckedChangeListener { _, checkedId ->
            fisicaoVirtual = when (checkedId) {
                R.id.TiendaFisica -> "fisica"
                R.id.TeindaVirtual -> "virtual"
                else -> ""
            }
        }

        bindingBottomShet.cerrar.setOnClickListener {
            dialog.dismiss()
        }
        btnApply.setOnClickListener {
            val isAllFieldsNotEmpty = listOf(
                nombreUser.text,
                dniUser.text,
                correoElectronico.text,
                telefono.text,
                nombreTienda.text,
                TelefonoTienda.text,
                categoriaTienda.text,
                subcategoriaTienda.text
            ).all { it.isNotEmpty() }

            if (isAllFieldsNotEmpty) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Enviaremos los datos que digitaste al WhatsApp de la empresa. La verificación puede tomar unos 15 minutos o más. ¿Estás seguro de continuar?")
                    .setPositiveButton("Sí") { _, _ ->
                        constantes.contactarWhatsapp(
                            "+51 937659216", "\n" +
                                    "|Geinz Cuenta Tienda:\n" +
                                    "|Campos enviados\n" +
                                    "|---------------Datos Personales---------------|\n" +
                                    "|Nombre: ${nombreUser.text}\n" +
                                    "|DNI: ${dniUser.text}\n" +
                                    "|Correo electronico: ${correoElectronico.text}\n" +
                                    "|Telefono: ${telefono.text}\n" +
                                    "|---------------Datos del Negocio o Tienda---------------|\n" +
                                    "|Nombre de la Tienda o negocio: ${nombreTienda.text}\n" +
                                    "|Virtual/Fisico: ${fisicaoVirtual}\n" +
                                    "|Localidad de la Tienda o Negocio: ${localidad}\n" +
                                    "|Telefono del Negocio o tienda: ${TelefonoTienda.text}\n" +
                                    "|Categoria de la Tienda o Negocio: ${categoriaTienda.text}\n" +
                                    "|Subcategoria de la Tienda o Negocio: ${subcategoriaTienda.text}\n" +
                                    "|----------------------------------|\n" +
                                    "", context
                        )
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                // Mostrar un mensaje de error si algún campo está vacío
                Toast.makeText(context, "Por favor, complete todos los campos requeridos.", Toast.LENGTH_SHORT).show()
            }
        }


        dialog.setContentView(view)

    }

    private fun actualizarSubcategorias(
        context: Context,
        categoria: String,
        subcategoriaTienda: AutoCompleteTextView?
    ) {
        val subcategorias = ArrayList<String>()
        constantesSubcategoriaszonasTiendas.obtenerySetearCat(subcategorias, categoria)
        val subcategoriaAdapter =
            ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, subcategorias)
        subcategoriaTienda?.setAdapter(subcategoriaAdapter)
    }
}