const { CloudTasksClient } = require("@google-cloud/tasks");
const tasksClient = new CloudTasksClient();

const PROJECT_ID = "geinzworkapp";
const LOCATION = "us-central1";
const QUEUE_NAME = "geinz-buffer-mensajes";
const INVOKER_SA_EMAIL = "geinz-tasks-invoker@geinzworkapp.iam.gserviceaccount.com";

// 👇 usar la URL de Cloud Run, no la de cloudfunctions.net
const URL_PROCESAR_BUFFER = "https://geinz-procesar-buffer-921389328767.us-central1.run.app";

// 👇 bajado de 2000 a 1300ms: suficiente para que WhatsApp agrupe
//    mensajes seguidos de una misma idea, sin sentirse lento cuando
//    el usuario manda un solo mensaje. Configurable por env var
//    para poder ajustar sin re-deployar código.
const VENTANA_DEBOUNCE_MS = Number(process.env.VENTANA_DEBOUNCE_MS) || 1300;

async function programarTareaDebounce({ numero_usuario, mensajeId }) {
  if (!numero_usuario || !mensajeId) {
    throw new Error(
      "programarTareaDebounce: numero_usuario y mensajeId son requeridos",
    );
  }

  const parent = tasksClient.queuePath(PROJECT_ID, LOCATION, QUEUE_NAME);
  const body = Buffer.from(
    JSON.stringify({ numero_usuario, mensajeId }),
  ).toString("base64");

  const scheduleTimeMs = Date.now() + VENTANA_DEBOUNCE_MS;

  const task = {
    httpRequest: {
      httpMethod: "POST",
      url: URL_PROCESAR_BUFFER,
      headers: { "Content-Type": "application/json" },
      body,
      oidcToken: {
        serviceAccountEmail: INVOKER_SA_EMAIL,
        audience: URL_PROCESAR_BUFFER,
      },
    },
    scheduleTime: {
      seconds: Math.floor(scheduleTimeMs / 1000),
      nanos: (scheduleTimeMs % 1000) * 1e6,
    },
    // 👇 si el endpoint no responde en 30s, Cloud Tasks lo marca como
    //    fallido y reintenta según la política de la queue, en vez de
    //    quedarse colgado indefinidamente
    dispatchDeadline: { seconds: 30 },
  };

  try {
    const [response] = await tasksClient.createTask({ parent, task });
    return response.name;
  } catch (e) {
    // Log explícito para poder diagnosticar cuotas de la queue,
    // permisos del service account, etc. sin perder el mensajeId
    console.error(
      "❌ [programarTareaDebounce] Falló creando task:",
      e.message,
      "| 👤:",
      numero_usuario,
      "| 📨 mensajeId:",
      mensajeId,
    );
    throw e;
  }
}

module.exports = { programarTareaDebounce, VENTANA_DEBOUNCE_MS };