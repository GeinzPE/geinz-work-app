// conectarFacebook.js
//
// Agrega esto a tu dashboard (donde tengas el botón "Conectar mi Fanpage").
// Requiere que en tu HTML tengas un <div id="fb-root"></div> antes de este
// script, y que hayas configurado tu App de Facebook en developers.facebook.com
// con el dominio de tu app en "Dominios de la app" y el SDK de JavaScript
// habilitado.

const FACEBOOK_APP_ID = "1271138448339142"; // el mismo App ID del backend, este SÍ es público
const CLOUD_FN_CONECTAR_FB =
  "https://us-central1-geinzworkapp.cloudfunctions.net/conectarFacebookPage";

// ── Cargar el SDK de Facebook dinámicamente ──
(function cargarFacebookSDK() {
  window.fbAsyncInit = function () {
    FB.init({
      appId: FACEBOOK_APP_ID,
      cookie: true,
      xfbml: false,
      version: "v19.0",
    });
  };

  const script = document.createElement("script");
  script.src = "https://connect.facebook.net/es_LA/sdk.js";
  script.async = true;
  script.defer = true;
  document.body.appendChild(script);
})();

// ── Función principal: se llama al presionar "Conectar mi Fanpage" ──
window.conectarMiFanpage = function () {
  FB.login(
    function (response) {
      if (response.authResponse) {
        const userAccessToken = response.authResponse.accessToken;
        obtenerPaginasYMostrarSelector(userAccessToken);
      } else {
        mostrarToast("Cancelaste el login de Facebook o no diste los permisos", "error");
      }
    },
    { scope: "pages_show_list,pages_manage_posts" },
  );
};

function obtenerPaginasYMostrarSelector(userAccessToken) {
  FB.api("/me/accounts", "GET", { access_token: userAccessToken }, function (res) {
    if (!res || res.error) {
      mostrarToast("No se pudo obtener la lista de páginas", "error");
      return;
    }

    const paginas = res.data || [];
    if (paginas.length === 0) {
      mostrarToast("No administras ninguna página de Facebook con este usuario", "error");
      return;
    }

    if (paginas.length === 1) {
      // Solo una página: conectar directo, sin preguntar
      confirmarConexion(paginas[0].id, userAccessToken);
      return;
    }

    // Varias páginas: mostrar selector simple
    mostrarSelectorDePaginas(paginas, userAccessToken);
  });
}

function mostrarSelectorDePaginas(paginas, userAccessToken) {
  const overlay = document.createElement("div");
  overlay.style.cssText =
    "position:fixed;inset:0;background:rgba(0,0,0,.7);z-index:9999;display:flex;align-items:center;justify-content:center;";
  overlay.innerHTML = `
    <div style="background:#15131c;border-radius:16px;padding:24px;max-width:360px;width:90%;">
      <h3 style="color:#fff;margin:0 0 14px;font-size:16px;">Elige tu Fanpage</h3>
      <div id="listaPaginasFb" style="display:flex;flex-direction:column;gap:8px;"></div>
    </div>
  `;
  document.body.appendChild(overlay);

  const lista = overlay.querySelector("#listaPaginasFb");
  paginas.forEach((p) => {
    const btn = document.createElement("button");
    btn.textContent = p.name;
    btn.style.cssText =
      "padding:12px;border-radius:10px;border:1px solid rgba(255,255,255,.1);background:#1e1e2a;color:#fff;cursor:pointer;text-align:left;";
    btn.onclick = () => {
      overlay.remove();
      confirmarConexion(p.id, userAccessToken);
    };
    lista.appendChild(btn);
  });
}

async function confirmarConexion(pageId, userAccessToken) {
  try {
    const result = await callFirebaseFunction(CLOUD_FN_CONECTAR_FB, {
      id_tienda: datosTienda.id_tienda,
      localidad: datosTienda.localidad,
      page_id: pageId,
      user_access_token: userAccessToken,
    });

    if (result.ok) {
      mostrarToast(`✅ ${result.mensaje}`);
    } else {
      mostrarToast(result.mensaje || "No se pudo conectar la página", "error");
    }
  } catch (err) {
    mostrarToast("Error conectando con Facebook", "error");
  }
}