const { onRequest } = require("firebase-functions/v2/https");
const admin = require("firebase-admin");

// Inicializar SDK si no ha sido inicializado antes
if (!admin.apps.length) {
  admin.initializeApp();
}

/**
 * Función Helper encargada de armar el payload compatible con Webpush y Android
 */
async function enviarNotificacionFCM_tienda({
  token,
  title,
  body,
  link,
  logo,
  idTienda,
  idAnuncio,
  tipo_notificacion,
  prioridad,
}) {
  const message = {
    token: token,

    // Configuración específica para Navegadores Web
    webpush: {
      headers: {
        Urgency: prioridad === "high" ? "high" : "normal",
      },
      notification: {
        title: title,
        body: body,
        icon: logo,
        badge: logo,
      },
      fcmOptions: {
        link: link,
      },
      data: {
        idTienda: idTienda || "",
        idAnuncio: idAnuncio || "",
        tipo_notificacion: tipo_notificacion || "",
      },
    },

    // Configuración específica para Android
    android: {
      priority: prioridad === "high" ? "high" : "normal",
      notification: {
        title: title,
        body: body,
        imageUrl: logo,
      },
      data: {
        link: link || "",
        idTienda: idTienda || "",
        idAnuncio: idAnuncio || "",
        tipo_notificacion: tipo_notificacion || "",
      },
    },

    // Payload de datos generales
    data: {
      title: title,
      body: body,
      link: link || "",
      idTienda: idTienda || "",
      idAnuncio: idAnuncio || "",
      tipo_notificacion: tipo_notificacion || "",
    },
  };

  return await admin.messaging().send(message);
}

/**
 * Cloud Function HTTP de prueba con valores estáticos
 */
exports.enviar_notificacion_con_solo_id = onRequest(async (req, res) => {
  // Manejo de Headers CORS para habilitar consumo desde la web
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(204).send("");
  }

  if (req.method !== "POST") {
    return res.status(405).send("Método no permitido");
  }

  try {
    // ⚠️ REEMPLAZA ESTE STRING POR UN TOKEN FCM WEB O ANDROID REAL PARA PROBAR
    const TOKEN_ESTATICO_DE_PRUEBA = "PEGA_AQUI_TU_FCM_TOKEN_DE_PRUEBA";

    // Parámetros con valores por defecto/estáticos si no vienen en la petición
    const id_tienda = req.body?.id_tienda || "TIENDA_DEMO_123";
    const nombre_negocio = req.body?.nombre_negocio || "Geinz Negocio Demo";

    await enviarNotificacionFCM_tienda({
      token: TOKEN_ESTATICO_DE_PRUEBA,
      title: `📢 ${nombre_negocio}, te están buscando`,
      body: `El asistente Daniel 🤖 recomendó a ${nombre_negocio} a un usuario interesado. Pero debido a que tu plantilla premium no tenía saldo activo, no se mostró el acceso directo a tu WhatsApp 📲 y se compartió tu perfil de Geinz 🏪. Verifica tu saldo 💳 para seguir conectando con clientes potenciales directo desde WhatsApp 🚀`,
      link: "https://geinztech.com/share?t=scr&id=rec",
      logo: "https://firebasestorage.googleapis.com/v0/b/geinzworkapp.appspot.com/o/logo_geinz_webp.webp?alt=media&token=aa1ef1df-1bcd-48f2-9cad-a85929c3a8d0",
      idTienda: id_tienda,
      idAnuncio: "",
      tipo_notificacion: "logo",
      prioridad: "high",
    });

    return res.status(200).json({
      ok: true,
      mensaje: "Notificación enviada correctamente al token estático",
    });
  } catch (error) {
    console.error("🔥 Error en enviar_notificacion_con_solo_id:", error);

    return res.status(500).json({
      ok: false,
      error: error.message,
    });
  }
});