package com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── COLORES ──────────────────────────────────────────────
private val ColorPurple       = Color(0xFF8400F0)
private val ColorPurpleLight  = Color(0xFFA855F7)
private val ColorPurpleSoft   = Color(0x268400F0)   // 15% alpha
private val ColorPurpleBorder = Color(0x408400F0)   // 25% alpha
private val ColorBg           = Color(0xFF1A1625)
private val ColorTitle        = Color(0xFFEDE8FF)
private val ColorDesc         = Color(0xFF7C6D99)
private val ColorChipText     = Color(0xFFC084FC)
private val ColorChipBg       = Color(0x268400F0)
private val ColorChipBorder   = Color(0x668400F0)
private val ColorDot          = Color(0xFFA855F7)

// ── PLAN ─────────────────────────────────────────────────
enum class PlanDaniel { GRATIS, PAGO }

// ── BANNER ───────────────────────────────────────────────
@Composable
fun BannerAsistenteDaniel(
    plan: PlanDaniel = PlanDaniel.GRATIS,
    chipVisible: Boolean = true,           // activa / desactiva el chip
    onClick: () -> Unit = {}
) {
    val planLabel = when (plan) {
        PlanDaniel.GRATIS -> "Gratis"
        PlanDaniel.PAGO   -> "Premium"
    }
    val planDotColor by animateColorAsState(
        targetValue = when (plan) {
            PlanDaniel.GRATIS -> Color(0xFF6EE7B7)   // verde suave
            PlanDaniel.PAGO   -> ColorDot             // púrpura
        },
        animationSpec = tween(300),
        label = "dotColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ColorBg)
            .clickable { onClick() }
    ) {
        // glow izquierdo
        Box(
            modifier = Modifier
                .size(width = 160.dp, height = 120.dp)
                .offset(x = (-30).dp, y = (-20).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ColorPurple.copy(alpha = 0.13f),
                            Color.Transparent
                        )
                    )
                )
        )

        // borde
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Transparent)
        ) {
            Surface(
                modifier = Modifier.matchParentSize(),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.5.dp,
                    color = ColorPurpleBorder
                )
            ) {}
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── CONTENIDO ──
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // chip plan
                if (chipVisible) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(ColorChipBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(planDotColor)
                        )
                        Text(
                            text = "Asistente activo · $planLabel",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorChipText,
                            letterSpacing = 0.4.sp
                        )
                    }
                    Spacer(Modifier.height(5.dp))
                }

                // título
                Text(
                    text = "Daniel, tu asistente de WhatsApp",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorTitle,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                // descripción
                Text(
                    text = "Responde mensajes automáticamente por ti, 24/7.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorDesc,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                // cta
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = ColorPurpleLight,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Configurar asistente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorPurpleLight
                    )
                }
            }

            // ── AVATAR ──
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // fade de izquierda a derecha (shadow entre texto e imagen)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ColorBg, Color.Transparent),
                                startX = 0f,
                                endX = 80f
                            )
                        )
                )

                // avatar circular
                Box(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B0764),
                                    Color(0xFF6B21A8),
                                    ColorPurple
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Ícono de robot como placeholder
                    // Reemplaza con AsyncImage si tienes URL de foto
                    Text(
                        text = "D",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE9D5FF)
                    )
                }
            }
        }
    }
}


