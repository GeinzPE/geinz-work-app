package com.geinzz.geinzwork.ui.adapters.ui.pantallas.bottom_sheet_general

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.data.model.servicio_comodidad
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.custom_textField_150
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.socios.boton_generador_por_IA
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_pantalla_socios
import com.geinzz.geinzwork.viewModels.viewmodel_eres_socio
import com.geinzz.geinzwork.viewModels.viewmodel_generaciones_IA

// ─── Colors ───────────────────────────────────────────────────────────────────
private val WaBackground       = Color(0xFF0B141A)
private val BubbleColor        = Color(0xFF202C33)
private val DividerColor       = Color(0xFF242626)
private val TextPrimary        = Color(0xFFE9EDEF)
private val TextMuted          = Color(0xFF8696A0)
private val LinkBlue           = Color(0xFF53BDEB)

private val WaBotSurface2      = Color(0xFF1A1A1A)
private val WaBotSurface3      = Color(0xFF252525)
private val WaBotBorder        = Color(0xFF2A2A2A)
private val WaBotGreenDeep     = Color(0xFF22B05B)
private val WaBotTextPrimary   = Color(0xFFFFFFFF)
private val WaBotTextSecondary = Color(0xFFE0E0E0)
private val WaBotTextMuted     = Color(0xFF888888)
private val WaBotTextHint      = Color(0xFFAAAAAA)

// ─── MODELS ───────────────────────────────────────────────────────────────────
data class PromoPreviewData(
    val badge: String       = "¡ÚLTIMOS DÍAS!",
    val title: String       = "Menú Final Four",
    val description: String = "Hamburguesa + 2 Piezas de Pollo en McCombo™ Mediano + 6 McNuggets™ + 1 Card Coleccionable",
    val price: String       = "S/ 23.90",
    val botMessage: String  = "Claro ¡Qué tal Benjamin! para tu helado, Alonso Peña RestoBar es una buena opción.",
    val credits: Int        = 10
)

enum class PlanType { FREE, PRO }
data class PlanFeature(val label: String, val included: Boolean)

// ─── MAIN ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppDanielBottomSheet(
    numero_whatsapp: String,
    sucategoira: List<String>,
    metodos_pago: modelo_pagos_tienda,
    servicios_comodidades: List<servicio_comodidad>,
    localidad_tienda: String,
    nombre_tienda: String,
    id_tienda: String,
    saldo_tienda: Number,
    onDismiss: () -> Unit,
    onActivate: () -> Unit = {},
    promoData: PromoPreviewData = PromoPreviewData(),
    // 🔥 imagen bot — vienen del padre
    imagen_subida_correctamente: Boolean = false,
    subiendo_imagen: Boolean = false,
    onImagenChange: (Uri?) -> Unit = {},
    usuario_borro_los_cambios: () -> Unit = {},
    onGuardarImagen: () -> Unit = {},
) {
    val viewmodel_generacones_IA: viewmodel_generaciones_IA = viewModel()
    val viewmodel: viewmodel_eres_socio = viewModel()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val fotoUrlInicial by viewmodel.estado_imagen_bot.collectAsState()
    LaunchedEffect(id_tienda) {
        viewmodel.obtener_descripcion_Seo_bot(id_tienda)
        viewmodel.resetear_valor_estado_whatsapp_subido_y_gemini()
        viewmodel.obtener_imange_bot(id_tienda)
    }

    val obtener_descripcion by viewmodel
        .obtener_estado_msje_whataspp_bot
        .collectAsState()

    var numero_cambiado    by remember { mutableStateOf(numero_whatsapp) }
    var msje_predeterminado by remember { mutableStateOf("") }

    LaunchedEffect(obtener_descripcion) {
        numero_cambiado = if (obtener_descripcion.numero_whatsapp.isNotBlank())
            obtener_descripcion.numero_whatsapp
        else
            numero_whatsapp
        msje_predeterminado = obtener_descripcion.msje_whatsapp
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = Color.Black,
        tonalElevation   = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF444444))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        BottomSheetContent(
            context                      = context,
            descripcion_negocio_seo      = obtener_descripcion.descripcion_seo,
            msje_personalizado_contacto  = msje_predeterminado,
            numero_whatsapp              = numero_cambiado,
            sucategoira                  = sucategoira,
            metodos_pago                 = metodos_pago,
            servicios_comodidades        = servicios_comodidades,
            localidad_tienda             = localidad_tienda,
            nombre_tienda                = nombre_tienda,
            id_tienda                    = id_tienda,
            saldo_tienda                 = saldo_tienda,
            viewmodel                    = viewmodel,
            viewmodel_generacones_IA     = viewmodel_generacones_IA,
            promoData                    = promoData,
            onActivate                   = onActivate,
            fotoUrlInicial               = fotoUrlInicial,
            // 🔥 imagen
            imagen_subida_correctamente  = imagen_subida_correctamente,
            subiendo_imagen              = subiendo_imagen,
            onImagenChange               = onImagenChange,
            usuario_borro_los_cambios    = usuario_borro_los_cambios,
            nuevo_cambio_whatsap         = { numero_cambiado = it },
            nuevo_cambio_whatsap_msje_predetermiando = { msje_predeterminado = it },
            guardar_cambios_firebase_numero_des = { data, tipo ->
                viewmodel.guadardar_descripcion_whattsapp_bot(id_tienda, localidad_tienda, data, tipo)
            },
            onGuardarImagen = onGuardarImagen
        )
    }
}

// ─── CONTENT ──────────────────────────────────────────────────────────────────
@Composable
private fun BottomSheetContent(
    context: Context,
    descripcion_negocio_seo: String,
    msje_personalizado_contacto: String,
    numero_whatsapp: String,
    sucategoira: List<String>,
    metodos_pago: modelo_pagos_tienda,
    servicios_comodidades: List<servicio_comodidad>,
    localidad_tienda: String,
    nombre_tienda: String,
    id_tienda: String,
    saldo_tienda: Number,
    viewmodel: viewmodel_eres_socio,
    viewmodel_generacones_IA: viewmodel_generaciones_IA,
    promoData: PromoPreviewData,
    onActivate: () -> Unit,
    fotoUrlInicial: String = "",
    // 🔥 imagen
    imagen_subida_correctamente: Boolean,
    subiendo_imagen: Boolean,
    onImagenChange: (Uri?) -> Unit,
    usuario_borro_los_cambios: () -> Unit,
    nuevo_cambio_whatsap: (String) -> Unit,
    nuevo_cambio_whatsap_msje_predetermiando: (String) -> Unit,
    guardar_cambios_firebase_numero_des: (String, String) -> Unit,
    onGuardarImagen: () -> Unit = {}
) {
    var mostrar_btn_guardar_chatbot_IA by remember { mutableStateOf(false) }
    var selectedPlan       by remember { mutableStateOf<PlanType?>(null) }
    var creditosAceptados  by remember { mutableStateOf(false) }
    var plantillaExpandida by remember { mutableStateOf(false) }
    var seoExpandido       by remember { mutableStateOf(false) }
    var descripcion_chat_bot by remember { mutableStateOf(descripcion_negocio_seo) }
    var descripcion_guardada by remember { mutableStateOf(descripcion_negocio_seo) }
    val scrollState = rememberScrollState()

    LaunchedEffect(descripcion_negocio_seo) {
        descripcion_chat_bot = descripcion_negocio_seo
        descripcion_guardada = descripcion_negocio_seo
    }

    val elTextoCambio by remember(descripcion_chat_bot, descripcion_guardada) {
        derivedStateOf { descripcion_chat_bot.trim() != descripcion_guardada.trim() }
    }

    val botonHabilitado = when (selectedPlan) {
        PlanType.FREE -> false
        PlanType.PRO  -> creditosAceptados
        null          -> false
    }

    val estado_subido_para_whatsapp_bot by viewmodel.estado_subido_desc_para_bot.collectAsState()
    val estado_descripcion_generadad_whatsapp by viewmodel_generacones_IA.estado_carga_generaciones_desk_whatsapp.collectAsState()

    val estaCargandoIA = estado_descripcion_generadad_whatsapp is viewmodel_generaciones_IA.Estado_generacion_IA_whsatp.loading

    LaunchedEffect(estado_descripcion_generadad_whatsapp) {
        when (estado_descripcion_generadad_whatsapp) {
            is viewmodel_generaciones_IA.Estado_generacion_IA_whsatp.succes -> {
                val data = (estado_descripcion_generadad_whatsapp as viewmodel_generaciones_IA.Estado_generacion_IA_whsatp.succes).txt
                if (!data.isNullOrEmpty()) {
                    descripcion_chat_bot = data
                    mostrar_btn_guardar_chatbot_IA = true
                }
            }
            else -> Unit
        }
    }

    var estadoAnterior by remember { mutableStateOf(false) }
    LaunchedEffect(estado_subido_para_whatsapp_bot) {
        if (estado_subido_para_whatsapp_bot && !estadoAnterior) {
            Toast.makeText(context, "Se actualizó la descripción correctamente", Toast.LENGTH_SHORT).show()
            descripcion_guardada = descripcion_chat_bot
            viewmodel.resetear_valor_estado_whatsapp_subido_y_gemini()
        }
        estadoAnterior = estado_subido_para_whatsapp_bot
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        SheetHeader()

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = WaBotBorder, thickness = 0.5.dp)
        Spacer(Modifier.height(12.dp))

        Text("Optimiza tu perfil de WhatsApp para ", color = WaBotTextHint, fontSize = 13.sp, lineHeight = 20.sp)
        Row {
            Text("Daniel", color = WaBotTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(", informa sobre tu negocio y atrae más clientes.", color = WaBotTextHint, fontSize = 13.sp, lineHeight = 20.sp)
        }

        SeoExpandableCard(
            estado_subido_para_whatsapp_bot = estado_subido_para_whatsapp_bot,
            elTextoCambio                   = elTextoCambio,
            mostrar_btn_guardar_chatbot_IA  = mostrar_btn_guardar_chatbot_IA,
            sucategoira                     = sucategoira,
            metodos_pago                    = metodos_pago,
            servicios_comodidades           = servicios_comodidades,
            localidad_tienda                = localidad_tienda,
            nombre_tienda                   = nombre_tienda,
            id_tienda                       = id_tienda,
            saldo_tienda                    = saldo_tienda,
            viewmodel                       = viewmodel,
            estaCargandoIA                  = estaCargandoIA,
            viewmodel_generacones_IA        = viewmodel_generacones_IA,
            seoExpandido                    = seoExpandido,
            onSeoToggle                     = { seoExpandido = !seoExpandido },
            descripcion_chat_bot            = descripcion_chat_bot,
            onDescripcionChange             = { descripcion_chat_bot = it }
        )

        Spacer(Modifier.height(14.dp))

        TemplateCard(
            msje_personalizado_contacto              = msje_personalizado_contacto,
            numero_whatsapp                          = numero_whatsapp,
            sucategoira                              = sucategoira,
            metodos_pago                             = metodos_pago,
            servicios_comodidades                    = servicios_comodidades,
            localidad_tienda                         = localidad_tienda,
            nombre_tienda                            = nombre_tienda,
            id_tienda                                = id_tienda,
            saldo_tienda                             = saldo_tienda,
            viewmodel                                = viewmodel,
            estaCargandoIA                           = estaCargandoIA,
            viewmodel_generacones_IA                 = viewmodel_generacones_IA,
            fotoUrlInicial                           = fotoUrlInicial,
            // 🔥 imagen
            imagen_subida_correctamente              = imagen_subida_correctamente,
            subiendo_imagen                          = subiendo_imagen,
            onImagenChange                           = onImagenChange,
            usuario_borro_los_cambios                = usuario_borro_los_cambios,
            promoData                                = promoData,
            plantillaExpandida                       = plantillaExpandida,
            onPlantillaToggle                        = { plantillaExpandida = !plantillaExpandida },
            selectedPlan                             = selectedPlan,
            onPlanSelected                           = { plan ->
                selectedPlan = plan
                if (plan == PlanType.FREE) creditosAceptados = false
            },
            creditosAceptados                        = creditosAceptados,
            onCreditosAceptadosChange                = { creditosAceptados = it },
            nuevo_cambio_whatsap                     = nuevo_cambio_whatsap,
            nuevo_cambio_whatsap_msje_predetermiando = nuevo_cambio_whatsap_msje_predetermiando,
            guardar_cambios_firebase_numero_des      = guardar_cambios_firebase_numero_des,
            onGuardarImagen = onGuardarImagen
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick   = onActivate,
            enabled   = botonHabilitado,
            modifier  = Modifier.fillMaxWidth().height(48.dp),
            shape     = RoundedCornerShape(14.dp),
            colors    = ButtonDefaults.buttonColors(
                containerColor         = MaterialTheme.colorScheme.primary,
                contentColor           = Color.White,
                disabledContainerColor = Color(0xFF1A1A1A),
                disabledContentColor   = Color(0xFF444444)
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = when (selectedPlan) {
                    PlanType.FREE -> "Plan gratis · Ya activo"
                    PlanType.PRO  -> if (creditosAceptados) "Activar Plan Pro" else "Acepta los créditos"
                    null          -> "Selecciona un plan"
                },
                fontSize      = 14.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 0.sp
            )
        }
    }
}

// ─── HEADER ───────────────────────────────────────────────────────────────────
@Composable
private fun SheetHeader() {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter            = painterResource(R.drawable.foto_perfil_daniel),
            contentDescription = null,
            modifier           = Modifier.size(44.dp).clip(CircleShape),
            contentScale       = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text("Asistente Daniel",     color = WaBotTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp)
            Text("WhatsApp Business AI", color = WaBotTextMuted,  fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(WaBotSurface2)
                .border(0.5.dp, WaBotBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(WaBotGreenDeep))
                Text("ACTIVO", color = WaBotGreenDeep, fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
        }
    }
}

// ─── TEMPLATE CARD ────────────────────────────────────────────────────────────
@Composable
private fun TemplateCard(
    msje_personalizado_contacto: String,
    numero_whatsapp: String,
    sucategoira: List<String>,
    metodos_pago: modelo_pagos_tienda,
    servicios_comodidades: List<servicio_comodidad>,
    localidad_tienda: String,
    nombre_tienda: String,
    id_tienda: String,
    saldo_tienda: Number,
    viewmodel: viewmodel_eres_socio,
    estaCargandoIA: Boolean,
    viewmodel_generacones_IA: viewmodel_generaciones_IA,
    fotoUrlInicial: String,
    // 🔥 imagen
    imagen_subida_correctamente: Boolean,
    subiendo_imagen: Boolean,
    onImagenChange: (Uri?) -> Unit,
    usuario_borro_los_cambios: () -> Unit,
    promoData: PromoPreviewData,
    plantillaExpandida: Boolean,
    onPlantillaToggle: () -> Unit,
    selectedPlan: PlanType?,
    onPlanSelected: (PlanType) -> Unit,
    creditosAceptados: Boolean,
    onCreditosAceptadosChange: (Boolean) -> Unit,
    nuevo_cambio_whatsap: (String) -> Unit,
    nuevo_cambio_whatsap_msje_predetermiando: (String) -> Unit,
    guardar_cambios_firebase_numero_des: (String, String) -> Unit,
    onGuardarImagen: () -> Unit = {},
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = WaBotSurface2),
        border   = BorderStroke(0.5.dp, WaBotBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth().clickable { onPlantillaToggle() },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier         = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(WaBotGreenDeep),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Plantilla Profesional",            color = WaBotTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Contacto directo · más interacción", color = WaBotTextMuted,  fontSize = 11.sp)
                }
                Icon(
                    imageVector        = if (plantillaExpandida) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = WaBotTextMuted,
                    modifier           = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = plantillaExpandida,
                enter   = expandVertically(tween(300), Alignment.Top) + fadeIn(tween(260)),
                exit    = shrinkVertically(tween(260), Alignment.Top) + fadeOut(tween(200))
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = WaBotSurface3, thickness = 0.5.dp)
                    Spacer(Modifier.height(12.dp))

                    PlansSection(
                        msje_personaliazdo_contaco               = msje_personalizado_contacto,
                        numero_whatsapp_tienda                   = numero_whatsapp,
                        onNumeroWhatsappChange                   = nuevo_cambio_whatsap,
                        onMensajePersonalizadoChange             = nuevo_cambio_whatsap_msje_predetermiando,
                        onGuardarCambios                         = guardar_cambios_firebase_numero_des,
                        sucategoira                              = sucategoira,
                        metodos_pago                             = metodos_pago,
                        servicios_comodidades                    = servicios_comodidades,
                        localidad_tienda                         = localidad_tienda,
                        nombre_tienda                            = nombre_tienda,
                        id_tienda                                = id_tienda,
                        saldo_tienda                             = saldo_tienda,
                        viewmodel                                = viewmodel,
                        estaCargandoIA                           = estaCargandoIA,
                        viewmodel_generacones_IA                 = viewmodel_generacones_IA,
                        fotoUrlInicial                           = fotoUrlInicial,
                        // 🔥 imagen
                        imagen_subida_correctamente              = imagen_subida_correctamente,
                        subiendo_imagen                          = subiendo_imagen,
                        onImagenChange                           = onImagenChange,
                        usuario_borro_los_cambios                = usuario_borro_los_cambios,
                        selectedPlan                             = selectedPlan,
                        onPlanSelected                           = onPlanSelected,
                        creditosAceptados                        = creditosAceptados,
                        onCreditosAceptadosChange                = onCreditosAceptadosChange,
                        onGuardarImagen = onGuardarImagen
                    )
                }
            }
        }
    }
}

// ─── PLANS SECTION ────────────────────────────────────────────────────────────
@Composable
private fun PlansSection(
    msje_personaliazdo_contaco: String,
    numero_whatsapp_tienda: String,
    onNumeroWhatsappChange: (String) -> Unit,
    onMensajePersonalizadoChange: (String) -> Unit,
    onGuardarCambios: (String, String) -> Unit,
    sucategoira: List<String>,
    metodos_pago: modelo_pagos_tienda,
    servicios_comodidades: List<servicio_comodidad>,
    localidad_tienda: String,
    nombre_tienda: String,
    id_tienda: String,
    saldo_tienda: Number,
    viewmodel: viewmodel_eres_socio,
    estaCargandoIA: Boolean,
    viewmodel_generacones_IA: viewmodel_generaciones_IA,
    fotoUrlInicial: String,
    // 🔥 imagen
    imagen_subida_correctamente: Boolean,
    subiendo_imagen: Boolean,
    onImagenChange: (Uri?) -> Unit,
    usuario_borro_los_cambios: () -> Unit,
    selectedPlan: PlanType?,
    onPlanSelected: (PlanType) -> Unit,
    creditosAceptados: Boolean,
    onCreditosAceptadosChange: (Boolean) -> Unit,
    onGuardarImagen: () -> Unit = {},
) {
    val previewPlan = selectedPlan ?: PlanType.FREE

    // 🔥 valores originales capturados una sola vez
    val numeroOriginal  = remember { numero_whatsapp_tienda }
    val mensajeOriginal = remember { msje_personaliazdo_contaco }

    val numeroCambio  = numero_whatsapp_tienda    != numeroOriginal
    val mensajeCambio = msje_personaliazdo_contaco != mensajeOriginal

    // ── Vista previa ──────────────────────────────────────────────────────────
    texto_generico_one_line("VISTA PREVIA EN WHATSAPP", color = WaBotTextMuted, style = MaterialTheme.typography.bodyMedium)
    spacer_vertical(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(WaBotSurface3)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        PreviewToggleButton(label = "Gratis", isActive = previewPlan == PlanType.FREE, modifier = Modifier.weight(1f)) { onPlanSelected(PlanType.FREE) }
        PreviewToggleButton(label = "Pro",    isActive = previewPlan == PlanType.PRO,  modifier = Modifier.weight(1f)) { onPlanSelected(PlanType.PRO)  }
    }

    Spacer(Modifier.height(10.dp))

    AnimatedVisibility(visible = previewPlan == PlanType.FREE, enter = fadeIn(tween(200)) + expandVertically(tween(220)), exit = fadeOut(tween(180)) + shrinkVertically(tween(200))) {
        WhatsAppBubbleRestauranteScreen()
    }

    AnimatedVisibility(visible = previewPlan == PlanType.PRO, enter = fadeIn(tween(200)) + expandVertically(tween(220)), exit = fadeOut(tween(180)) + shrinkVertically(tween(200))) {
        // 🔥 los 4 parámetros de imagen llegan correctamente aquí
        WhatsAppBubbleScreen(
            fotoUrlInicial              = fotoUrlInicial,
            imagen_subida_correctamente = imagen_subida_correctamente,
            subiendo_imagen             = subiendo_imagen,
            onImagenChange              = onImagenChange,
            usuario_borro_los_cambios   = usuario_borro_los_cambios,
            onGuardarImagen = onGuardarImagen
        )
    }

    Spacer(Modifier.height(10.dp))
    HorizontalDivider(color = WaBotSurface3, thickness = 0.5.dp)
    Spacer(Modifier.height(10.dp))

    // ── Selector de plan ──────────────────────────────────────────────────────
    texto_generico_one_line("ELIGE TU PLAN", color = WaBotTextMuted, style = MaterialTheme.typography.bodyMedium)
    spacer_vertical(10.dp)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlanCard(
            modifier   = Modifier.weight(1f).fillMaxHeight(),
            planType   = PlanType.FREE,
            isSelected = selectedPlan == PlanType.FREE,
            isFeatured = false,
            badge      = null,
            planName   = "Plan gratis",
            planPrice  = "Sin costo",
            features   = listOf(
                PlanFeature("Nombre del negocio",     true),
                PlanFeature("Estado abierto/cerrado", true),
                PlanFeature("Métodos de pago",        true),
                PlanFeature("Comodidades básicas",    true),
                PlanFeature("Link a tu perfil",       true),
                PlanFeature("Imagen dinámica",        false),
                PlanFeature("Promociones",            false),
                PlanFeature("Botón de contacto",      false)
            ),
            onClick = { onPlanSelected(PlanType.FREE) }
        )
        PlanCard(
            modifier   = Modifier.weight(1f).fillMaxHeight(),
            planType   = PlanType.PRO,
            isSelected = selectedPlan == PlanType.PRO,
            isFeatured = false,
            badge      = "RECOMENDADO",
            planName   = "Plan Pro",
            planPrice  = "10 cred / recomend.",
            features   = listOf(
                PlanFeature("Todo lo del gratis",     true),
                PlanFeature("Imagen / logo dinámico", true),
                PlanFeature("Promociones activas",    true),
                PlanFeature("Formato profesional",    true),
                PlanFeature("Estado en tiempo real",  true),
                PlanFeature("Botón contacto WA",      true),
                PlanFeature("Botón ver perfil app",   true),
                PlanFeature("Texto más ordenado",     true)
            ),
            onClick = { onPlanSelected(PlanType.PRO) }
        )
    }

    // ── Sección PRO ───────────────────────────────────────────────────────────
    AnimatedVisibility(
        visible = selectedPlan == PlanType.PRO,
        enter   = fadeIn(tween(220)) + expandVertically(tween(260), Alignment.Top),
        exit    = fadeOut(tween(180)) + shrinkVertically(tween(200), Alignment.Top)
    ) {
        Column {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = WaBotSurface3, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            texto_generico_one_line("DATOS DE CONTACTO PRO", color = WaBotTextMuted, style = MaterialTheme.typography.bodyMedium)
            spacer_vertical(10.dp)

            // ── Campo número ──────────────────────────────────────────────────
            texto_generico_multilinea("Número de WhatsApp al que será redirigido el usuario al hacer clic.", style = MaterialTheme.typography.labelMedium)

            custom_textField_150(
                mostrar_contado_palabras = false,
                rounder                  = 25,
                value                    = numero_whatsapp_tienda,
                onValueChange            = onNumeroWhatsappChange,
                labelText                = "Tu número de WhatsApp",
                placeholderText          = "Ej: 987654321",
                keyboardOptions          = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            AnimatedVisibility(
                visible = numeroCambio,
                enter   = fadeIn(tween(220)) + expandVertically(tween(240)),
                exit    = fadeOut(tween(180)) + shrinkVertically(tween(200))
            ) {
                Button(
                    onClick  = { onGuardarCambios(numero_whatsapp_tienda, "wsap") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = WaBotGreenDeep)
                ) {
                    Text("Guardar número", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ── Campo mensaje ─────────────────────────────────────────────────
            texto_generico_multilinea("Mensaje predeterminado con el que el usuario iniciará la conversación", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 10.dp))

            custom_textField_150(
                rounder         = 25,
                value           = msje_personaliazdo_contaco,
                onValueChange   = onMensajePersonalizadoChange,
                labelText       = "Mensaje personalizado de contacto",
                placeholderText = "Ej: Hola! Vi tu negocio en Geinz..."
            )

            AnimatedVisibility(
                visible = mensajeCambio,
                enter   = fadeIn(tween(220)) + expandVertically(tween(240)),
                exit    = fadeOut(tween(180)) + shrinkVertically(tween(200))
            ) {
                Button(
                    onClick  = { onGuardarCambios(msje_personaliazdo_contaco, "msje") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = WaBotGreenDeep)
                ) {
                    Text("Guardar mensaje", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            spacer_vertical(15.dp)
            HorizontalDivider(color = WaBotSurface3, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // ── Info costos ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF141414))
                    .border(0.5.dp, WaBotBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("−10 ", color = WaBotGreenDeep, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Image(painter = painterResource(R.drawable.icon_monedas_3d), contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(" x recomendacion.", color = if (creditosAceptados) WaBotTextMuted else Color(0xFF555555), fontSize = 16.sp)
                }
                HorizontalDivider(color = WaBotSurface3, thickness = 0.5.dp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("−5 ", color = WaBotGreenDeep, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Image(painter = painterResource(R.drawable.icon_monedas_3d), contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(" x cliente potencial.", color = if (creditosAceptados) WaBotTextMuted else Color(0xFF555555), fontSize = 16.sp)
                }
            }

            spacer_vertical(5.dp)

            texto_generico_multilinea(
                "Ejemplo: Daniel recomendó tu negocio 20 veces y 5 clientes potenciales te escribieron directo a WhatsApp gracias al Plan Pro. Total: 225 créditos = S/ 2.25.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(12.dp))

            // ── Checkbox créditos ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (creditosAceptados) Color(0xFF0F2A1A) else Color(0xFF141414))
                    .border(0.5.dp, if (creditosAceptados) WaBotGreenDeep else WaBotBorder, RoundedCornerShape(10.dp))
                    .clickable { onCreditosAceptadosChange(!creditosAceptados) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (creditosAceptados) WaBotGreenDeep else Color.Transparent)
                        .border(1.5.dp, if (creditosAceptados) WaBotGreenDeep else WaBotTextMuted, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (creditosAceptados) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "Acreditar créditos del saldo actual",
                        color      = if (creditosAceptados) Color(0xFFDDDDDD) else WaBotTextMuted,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── PLAN CARD ────────────────────────────────────────────────────────────────
@Composable
private fun PlanCard(
    modifier: Modifier = Modifier,
    planType: PlanType,
    isSelected: Boolean,
    isFeatured: Boolean,
    badge: String?,
    planName: String,
    planPrice: String,
    features: List<PlanFeature>,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Card(
            onClick  = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(12.dp),
            colors   = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
            border   = BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) WaBotGreenDeep else WaBotBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(20.dp), contentAlignment = Alignment.Center) {
                    if (badge != null) {
                        Box(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                .background(WaBotGreenDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(planName, color = Color(0xFFDDDDDD), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                if (planType == PlanType.FREE) {
                    Text("Sin costo", color = WaBotTextMuted, fontSize = 14.sp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("10 ", color = WaBotGreenDeep, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Image(painter = painterResource(R.drawable.icon_monedas_3d), contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(" x 1 recomendacion.", color = WaBotTextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                features.forEach { feature ->
                    Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(
                            imageVector        = if (feature.included) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint               = if (feature.included) WaBotGreenDeep else Color(0xFF333333),
                            modifier           = Modifier.size(12.dp).padding(top = 2.dp)
                        )
                        Text(text = feature.label, color = if (feature.included) Color(0xFF999999) else Color(0xFF444444), fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// ─── PREVIEW TOGGLE ───────────────────────────────────────────────────────────
@Composable
private fun PreviewToggleButton(
    label: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFF1A3A2A) else Color.Transparent)
            .border(if (isActive) 0.5.dp else 0.dp, if (isActive) WaBotGreenDeep else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = if (isActive) WaBotGreenDeep else WaBotTextMuted, fontSize = 13.sp, fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal)
    }
}

// ─── BUBBLE PRO ───────────────────────────────────────────────────────────────
@Composable
fun WhatsAppBubbleScreen(
    fotoUrlInicial: String = "",
    imagen_subida_correctamente: Boolean = false,
    subiendo_imagen: Boolean = false,
    onImagenChange: (Uri?) -> Unit = {},
    usuario_borro_los_cambios: () -> Unit = {},
            onGuardarImagen: () -> Unit = {}
) {
    Box(
        modifier         = Modifier.fillMaxWidth().background(WaBackground).padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        MessageBubble(
            fotoUrlInicial              = fotoUrlInicial,
            imagen_subida_correctamente = imagen_subida_correctamente,
            subiendo_imagen             = subiendo_imagen,
            onImagenChange              = onImagenChange,
            usuario_borro_los_cambios   = usuario_borro_los_cambios,
            onGuardarImagen = onGuardarImagen
        )
    }
}

@Composable
fun MessageBubble(
    fotoUrlInicial: String = "",
    imagen_subida_correctamente: Boolean = false,
    subiendo_imagen: Boolean = false,
    onImagenChange: (Uri?) -> Unit = {},
    usuario_borro_los_cambios: () -> Unit = {},
    onGuardarImagen: () -> Unit = {}
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Box(modifier = Modifier.size(width = 8.dp, height = 8.dp).align(Alignment.TopEnd)) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawPath(path = Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(0f, size.height); close() }, color = BubbleColor)
            }
        }
        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(BubbleColor)
        ) {
            // 🔥 toda la lógica de imagen delegada al componente reutilizable
            constantes_pantalla_socios.Box_para_imagen_general_de_Bot_whatsapp(
                imagen_subida_correctamente = imagen_subida_correctamente,
                subiendo_imagen             = subiendo_imagen,
                imagenInicial = fotoUrlInicial.takeIf { it.isNotBlank() },
                onImagenChange              = onImagenChange,
                usuario_borro_los_cambios   = usuario_borro_los_cambios,
                        onGuardarImagen             = onGuardarImagen

            )
            MessageBody()
            Divider(color = DividerColor, thickness = 0.5.dp)
            LinkRow(label = "Ver perfil")
            Divider(color = DividerColor, thickness = 0.5.dp)
            LinkRow(label = "Contactar")
        }
    }
}

// ─── MESSAGE BODY ─────────────────────────────────────────────────────────────
@Composable
fun MessageBody() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
        Spacer(Modifier.height(3.dp))
        Text(
            text       = "Descripción optimizada por Daniel IA para potenciar la visibilidad y conversión de tu negocio. Basado en análisis SEO, comportamiento de clientes y millones de datos entrenados para generar mensajes más atractivos, estratégicos y orientados a maximizar el ROI y atraer más clientes para ti.",
            color      = TextPrimary,
            fontSize   = 15.sp,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Geinz", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("18:41", color = TextMuted, fontSize = 12.sp)
        }
    }
}

// ─── LINK ROW ─────────────────────────────────────────────────────────────────
@Composable
fun LinkRow(label: String) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Filled.OpenInNew, contentDescription = null, tint = WaBotGreenDeep, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = WaBotGreenDeep, fontSize = 14.sp)
    }
}

// ─── BUBBLE FREE ──────────────────────────────────────────────────────────────
@Composable
fun WhatsAppBubbleRestauranteScreen() {
    Box(modifier = Modifier.fillMaxWidth().background(WaBackground).padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        BubbleRestaurante()
    }
}

@Composable
fun BubbleRestaurante() {
    Box(contentAlignment = Alignment.TopEnd) {
        Canvas(modifier = Modifier.size(width = 8.dp, height = 8.dp).align(Alignment.TopEnd)) {
            drawPath(path = Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(0f, size.height); close() }, color = BubbleColor)
        }
        Column(
            modifier = Modifier
                .widthIn(max = 295.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 0.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(BubbleColor)
        ) {
            MensajeBody()
        }
    }
}

@Composable
private fun MensajeBody() {
    val uriHandler = LocalUriHandler.current
    val url = "https://geinzworkapp.web.app/share?t=ti&id=JHqbs7ttVXRnsIqsEGWS&l=barranca&c=comida+y+restaurantes"

    val textoConLink = buildAnnotatedString {
        withStyle(SpanStyle(color = TextPrimary, fontSize = 15.sp)) {
            append("Tu contacto personal no será mostrado públicamente. Los clientes podrán comunicarse contigo de forma segura desde Geinz. ✨")
        }
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(SpanStyle(color = LinkBlue, fontSize = 14.sp, textDecoration = TextDecoration.Underline)) { append(url) }
        pop()
    }

    Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 4.dp)) {
        ClickableText(
            text    = textoConLink,
            style   = TextStyle(lineHeight = 23.sp),
            onClick = { offset -> textoConLink.getStringAnnotations("URL", offset, offset).firstOrNull()?.let { uriHandler.openUri(it.item) } }
        )
        Spacer(Modifier.height(4.dp))
        Text("21:03", color = TextMuted, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.End).padding(bottom = 4.dp))
    }
}

// ─── SEO EXPANDABLE CARD ──────────────────────────────────────────────────────
@Composable
private fun SeoExpandableCard(
    estado_subido_para_whatsapp_bot: Boolean,
    elTextoCambio: Boolean,
    mostrar_btn_guardar_chatbot_IA: Boolean,
    sucategoira: List<String>,
    metodos_pago: modelo_pagos_tienda,
    servicios_comodidades: List<servicio_comodidad>,
    localidad_tienda: String,
    nombre_tienda: String,
    id_tienda: String,
    saldo_tienda: Number,
    viewmodel: viewmodel_eres_socio,
    estaCargandoIA: Boolean,
    viewmodel_generacones_IA: viewmodel_generaciones_IA,
    seoExpandido: Boolean,
    onSeoToggle: () -> Unit,
    descripcion_chat_bot: String,
    onDescripcionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = WaBotSurface2),
        border   = BorderStroke(0.5.dp, WaBotBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth().clickable { onSeoToggle() },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier         = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Descripción SEO",                             color = WaBotTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Optimiza cómo Daniel recomienda tu negocio",  color = WaBotTextMuted,  fontSize = 11.sp)
                }
                Icon(
                    imageVector        = if (seoExpandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = WaBotTextMuted,
                    modifier           = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = seoExpandido,
                enter   = expandVertically(tween(300), Alignment.Top) + fadeIn(tween(260)),
                exit    = shrinkVertically(tween(260), Alignment.Top) + fadeOut(tween(200))
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = WaBotSurface3, thickness = 0.5.dp)
                    Spacer(Modifier.height(14.dp))

                    texto_generico_multilinea("Mejora la información de tu negocio para que Daniel IA pueda recomendarte mejor dentro de WhatsApp y atraer más clientes.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(14.dp))

                    custom_textField_150(
                        rounder         = 18,
                        value           = descripcion_chat_bot,
                        onValueChange   = onDescripcionChange,
                        labelText       = "Descripción SEO para WhatsApp",
                        placeholderText = "Ej: Restaurante familiar especializado en parrillas..."
                    )

                    Spacer(Modifier.height(14.dp))

                    val mostrarBotonGuardar = (mostrar_btn_guardar_chatbot_IA || elTextoCambio) && !estado_subido_para_whatsapp_bot

                    AnimatedVisibility(
                        visible = mostrarBotonGuardar,
                        enter   = fadeIn(tween(220)) + expandVertically(tween(240)),
                        exit    = fadeOut(tween(180)) + shrinkVertically(tween(200))
                    ) {
                        Column {
                            Button(
                                onClick  = { viewmodel.guadardar_descripcion_whattsapp_bot(id_tienda, localidad_tienda, descripcion_chat_bot, "desc") },
                                modifier = Modifier.fillMaxWidth(),
                                shape    = CircleShape,
                                colors   = ButtonDefaults.buttonColors(containerColor = WaBotGreenDeep)
                            ) {
                                Text("Guardar cambios", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            spacer_vertical(10.dp)
                        }
                    }

                    spacer_vertical(10.dp)

                    boton_generador_por_IA(
                        cargando      = estaCargandoIA,
                        onclick       = {
                            val data_para_ia = viewmodel.prepararInputParaIA(sucategoira, metodos_pago, servicios_comodidades)
                            Log.d("data_para_ia", data_para_ia)
                            viewmodel_generacones_IA.obtener_descripcion_generada_con_datos(data_para_ia, localidad_tienda, nombre_tienda, id_tienda, "30", saldo_tienda.toInt())
                        },
                        texto_button  = "Generar descripción con IA",
                        cantidad_monedas = "30"
                    )
                }
            }
        }
    }
}