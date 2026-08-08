# ==============================================================
# Script de deploy FUNCIÓN POR FUNCIÓN para Firebase Functions
# - Omite automáticamente las funciones ya desplegadas (lista $YaDesplegadas)
# - Si una función falla, la registra en $Diferidas y SIGUE con las demás
#   (no aborta todo el script)
# - Al final imprime un resumen de lo que quedó pendiente/fallido
# ==============================================================

# --------- FUNCIONES YA DESPLEGADAS CON ÉXITO (se omiten) ---------
$YaDesplegadas = @(
    "geinz_webhook_principal",
    "geinz_procesar_buffer",
    "geinz_webhook_telegram",
    "geinz_webhook_principal_scag_ai",
    "geinz_webhook_telegram_scag_ai",
    "confirmarPago",
    "crearPromocion",
    "pagar_plan__usuario",
    "descontar_creditos_tienda"
)

# --------- Acumulador global de funciones que fallaron ---------
$global:Diferidas = @()

function Deploy-Grupo {
    param(
        [string]$NombreGrupo,
        [string[]]$Funciones
    )

    Write-Host ""
    Write-Host "==============================================" -ForegroundColor Yellow
    Write-Host "🚀 Desplegando: $NombreGrupo ($($Funciones.Count) funciones)" -ForegroundColor Yellow
    Write-Host "==============================================" -ForegroundColor Yellow

    foreach ($fn in $Funciones) {

        if ($YaDesplegadas -contains $fn) {
            Write-Host ""
            Write-Host "  ⏭️  $fn ... omitida (ya estaba desplegada)" -ForegroundColor DarkGray
            continue
        }

        Write-Host ""
        Write-Host "  → $fn ..." -ForegroundColor Cyan

        firebase deploy --only "functions:$fn"

        if ($LASTEXITCODE -ne 0) {
            Write-Host "  ❌ ERROR desplegando '$fn'. Se registra y se continúa con las demás." -ForegroundColor Red
            $global:Diferidas += $fn
            Start-Sleep -Seconds 3
            continue
        }

        Write-Host "  ✅ $fn desplegada" -ForegroundColor Green
        Start-Sleep -Seconds 5
    }

    Write-Host "✅ $NombreGrupo procesado" -ForegroundColor Green
    Write-Host "Esperando 15 segundos antes del siguiente grupo..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
}

# ------------------- GRUPOS -------------------

Deploy-Grupo -NombreGrupo "Grupo 1: Webhooks críticos" -Funciones @(
    "geinz_webhook_principal",
    "geinz_procesar_buffer",
    "geinz_webhook_telegram",
    "geinz_webhook_principal_scag_ai",
    "geinz_webhook_telegram_scag_ai"
)

# ⏭️ Grupo 2 omitido: funciones no exportadas en index.js actual

Deploy-Grupo -NombreGrupo "Grupo 3: Pagos y créditos" -Funciones @(
    "confirmarPago",
    "crearPromocion",
    "pagar_plan__usuario",
    "descontar_creditos_tienda",
    "obtener_creditos_tienda",
    "agregar_pago_para_el_usuario_tienda"
)

Deploy-Grupo -NombreGrupo "Grupo 4: Búsquedas y listados" -Funciones @(
    "buscar_por_nombre__tienda",
    "buscar_por_categoria_subcateogira",
    "buscarTiendasSmart",
    "buscarTiendas",
    "buscar_tienda_por_categorias_y_subcategoria",
    "filtrar_por_datos_chat_bot",
    "filtrar_por_datos",
    "busqueda_algolia_turismo_bot_geinz"
)

Deploy-Grupo -NombreGrupo "Grupo 5: SSR y compartir" -Funciones @(
    "share",
    "perfilSSR",
    "turismoSSR",
    "sitemap",
    "staticSSR"
)

Deploy-Grupo -NombreGrupo "Grupo 6: Notificaciones y alertas" -Funciones @(
    "enviarNotificacion",
    "enviar_notificacion_con_solo_id",
    "enviar_notificacion_deuda_acumulada",
    "resetearEstadoNotificacionesYPanel",
    "verificarMinimoSeguidores",
    "alertaSaldoBajo",
    "enviarMensajeManual"
)

Deploy-Grupo -NombreGrupo "Grupo 7: Facebook / Algolia / TTS / utilidades" -Funciones @(
    "conectarFacebookPage",
    "publicarEnFacebookOrganico",
    "syncProductoAlgolia",
    "syncCategoriaAlgolia",
    "textoAVozOpenAI",
    "textoAVozn8n_elevenlabs_2",
    "transcribirAudio",
    "recognizeSpeech",
    "textToSpeechIA",
    "textToSpeechIA_con_params",
    "extraerDatos",
    "extraer_datos_de_texto_completo"
)

Deploy-Grupo -NombreGrupo "Grupo 8: Resto (menos crítico)" -Funciones @(
    "obtener_lugares_seguros",
    "obtener_lugares_turisticos_directos",
    "tiendasGeo",
    "agregar_usuario_de_geinz_bot",
    "obtenerCategorias",
    "obtener_subcategoira_de_cat",
    "agregar_error_firebase_bot",
    "agregar_historial_usuario",
    "verificar_usuario_asistente",
    "banUser",
    "geinz_aviso_qr_escaneado",
    "onPromocionChange",
    "eliminarPromocionesExpiradasCadaMinuto",
    "limpiarPromosExpiradas"
)

Write-Host ""
if ($global:Diferidas.Count -eq 0) {
    Write-Host "🎉 TODAS LAS FUNCIONES SE DESPLEGARON CORRECTAMENTE 🎉" -ForegroundColor Green
} else {
    Write-Host "⚠️  Terminado con funciones pendientes de reintentar:" -ForegroundColor Yellow
    foreach ($fn in $global:Diferidas) {
        Write-Host "   - $fn" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Cuando las corrijas, puedes redesplegarlas individualmente con:" -ForegroundColor Yellow
    Write-Host '   firebase deploy --only "functions:NOMBRE_FUNCION"' -ForegroundColor Cyan
}