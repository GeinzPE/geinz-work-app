package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

// ---------- ANDROID GRAPHICS (QR / BITMAP) ----------
import android.content.Context
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn


// ---------- COMPOSE ----------
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R

// ---------- ZXING (QR) ----------
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private var logoQrCache: Bitmap? = null


@Composable
fun generar_qr_ubi_tinda(
    content: String,
    bottomText: String,
    sizeDp: Int = 220,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(content) {
        cargando = true
        bitmap = withContext(Dispatchers.Default) {
            generarQrBitmapAltaCalidad(context, content)
        }
        cargando = false
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier.size(sizeDp.dp),
            contentAlignment = Alignment.Center
        ) {

            // 🔄 LOADING
            this@Column.AnimatedVisibility(
                visible = cargando,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }

            // ✅ QR GENERADO
            this@Column.AnimatedVisibility(
                visible = !cargando && bitmap != null,
                enter = fadeIn() + scaleIn(initialScale = 0.92f),
                exit = fadeOut()
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR",
                        modifier = Modifier
                            .size(sizeDp.dp)
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        texto_generico_multilinea(
            bottomText,
            MaterialTheme.typography.bodyMedium,
            Color = androidx.compose.ui.graphics.Color.White
        )
    }
}



fun generarQrBitmapAltaCalidad(
    context: Context,
    content: String
): Bitmap {
    val size = 900

    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
        EncodeHintType.MARGIN to 4
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

    // ===== LOGO NEGRO (CACHEADO) =====
    val logo = logoQrCache ?: BitmapFactory.decodeResource(
        context.resources,
        R.drawable.logo_para_qr
    ).also { logoQrCache = it }

    val logoSize = (size * 0.14f).toInt()
    val cx = size / 2f
    val cy = size / 2f
    val radius = logoSize / 2f

    canvas.drawCircle(
        cx, cy, radius + 18,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
    )

    canvas.drawCircle(
        cx, cy, radius + 12,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    )

    val logoScaled = Bitmap.createScaledBitmap(
        logo, logoSize, logoSize, true
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

    return bmp
}



