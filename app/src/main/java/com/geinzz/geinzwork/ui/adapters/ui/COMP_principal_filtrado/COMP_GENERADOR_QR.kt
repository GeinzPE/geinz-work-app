package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

// ---------- ANDROID GRAPHICS (QR / BITMAP) ----------
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter


// ---------- COMPOSE ----------
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R

// ---------- ZXING (QR) ----------
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
@Composable
fun generar_qr_ubi_tinda(
    content: String,
    bottomText: String,
    sizeDp: Int = 220,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bitmap = remember(content) {
        val size = 900

        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, // 🔥 CLAVE
            EncodeHintType.MARGIN to 4 // 🔥 CLAVE
        )

        val bitMatrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        val module = size / bitMatrix.width.toFloat()

        // ===== QR CUADRADO PURO (LECTURA INSTANTÁNEA) =====
        for (x in 0 until bitMatrix.width) {
            for (y in 0 until bitMatrix.height) {
                if (bitMatrix[x, y]) {
                    canvas.drawRect(
                        x * module,
                        y * module,
                        (x + 1) * module,
                        (y + 1) * module,
                        paint
                    )
                }
            }
        }

        // ===== LOGO PEQUEÑO ESTILO YAPE =====
        val logo = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.logo_para_qr // PNG transparente
        )

        val logoSize = (size * 0.14f).toInt() // 🔥 NO MÁS
        val cx = size / 2f
        val cy = size / 2f
        val radius = logoSize / 2f

        // Borde negro
        canvas.drawCircle(
            cx,
            cy,
            radius + 18,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
            }
        )

        // Fondo blanco
        canvas.drawCircle(
            cx,
            cy,
            radius + 12,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
            }
        )

        val logoScaled = Bitmap.createScaledBitmap(
            logo,
            logoSize,
            logoSize,
            true
        )

        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            colorFilter = PorterDuffColorFilter(
                Color.BLACK,
                PorterDuff.Mode.SRC_IN
            )
        }

        canvas.drawBitmap(
            logoScaled,
            cx - radius,
            cy - radius,
            logoPaint
        )

        bmp
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR",
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        texto_generico_multilinea(bottomText, MaterialTheme.typography.bodyMedium, Color = androidx.compose.ui.graphics.Color.White)

    }
}



