package com.geinzz.geinzwork.vistaTiendas

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityGenerarQrpedidoArticulosBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class generarQRPedidoArticulos : AppCompatActivity() {
    private lateinit var binding:ActivityGenerarQrpedidoArticulosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityGenerarQrpedidoArticulosBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var codigoPedido = intent.getStringExtra("codigoOrden")
        var NombreTienda = intent.getStringExtra("nombreTiena")
        var nombreUSer = intent.getStringExtra("user")
        var numeroUser = intent.getStringExtra("numerouser")
        var hora = intent.getStringExtra("hora")
        var fechaReservaUser = intent.getStringExtra("fecha")
        var estado = intent.getStringExtra("estado")
        var metodoEntrega = intent.getStringExtra("metodoentrega")
        var metodoPago = intent.getStringExtra("pagometodo")
        var total = intent.getStringExtra("total")
        var iduser = intent.getStringExtra("iduser")
        var direccionUser = intent.getStringExtra("direccion")
        var idTienda = intent.getStringExtra("idTienda")
        var prouctos = intent.getBundleExtra("prouctos")
        var descripcion = intent.getStringExtra("descripcion")
        var localidadUSER = intent.getStringExtra("localidadUSER")
        var localidadTienda = intent.getStringExtra("localidadTienda")

//        if (prouctos != null) {
//            val productos = mutableMapOf<String, Int>()
//            for (key in prouctos.keySet()) {
//                productos[key] = prouctos.getInt(key)
//            }
            val qrContent = """
            Nombre: $nombreUSer
            Número: $numeroUser
            Direccion:$direccionUser
            Fecha de Reserva: $fechaReservaUser
            Hora de Reserva: $hora
          
            Total:$total
            Metodo de pago: $metodoPago
            Metodo de entrega:$metodoEntrega
            Descripcion:$descripcion
            CodigoPedido:$codigoPedido
            Estado:$estado
            NombreTienda:$NombreTienda
            IDuser:$iduser
            IDTienda:$idTienda
            LocalidaddUsuario:$localidadUSER
            LocalidadTienda:$localidadTienda
        """.trimIndent()
            // Generar el código QR
            val qrBitmap = generateQRCode(qrContent)

            // Mostrar el código QR en un ImageView
            val qrImageView = binding.qrImageView
            qrImageView.setImageBitmap(qrBitmap)
       // }
    }

    private fun generateQRCode(content: String): Bitmap? {
        val qrCodeWriter = QRCodeWriter()
        return try {
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix.get(
                                x,
                                y
                            )
                        ) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                    )
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

}