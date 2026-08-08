const { onDocumentWritten } = require('firebase-functions/v2/firestore');
const admin = require('firebase-admin');
const algoliasearch = require('algoliasearch');

// admin.initializeApp() NO se llama aquí -> asume que ya lo hiciste en tu index.js principal.

const ALGOLIA_APP_ID = process.env.ALGOLIA_APP_ID;
const ALGOLIA_ADMIN_API_KEY = process.env.ALGOLIA_ADMIN_API_KEY;
const ALGOLIA_INDEX_NAME = process.env.ALGOLIA_INDEX_MENU || 'restaurante_menu_items';

function getAlgoliaIndex() {
  const client = algoliasearch(ALGOLIA_APP_ID, ALGOLIA_ADMIN_API_KEY);
  return client.initIndex(ALGOLIA_INDEX_NAME);
}

/**
 * Trigger sobre CUALQUIER producto de CUALQUIER tienda/categoría.
 *
 * Ruta NUEVA (jerarquía país/departamento/provincia/distrito):
 *   Tiendas/{pais}/departamento/{departamento}/provincia/{provincia}
 *          /distrito/{localidad}/{subcoleccion}/{tiendaDocId}
 *          /categorias/{categoriaId}/productos/{productoId}
 *
 * {pais}/{departamento}/{provincia} van como comodín (no hardcodeados)
 * para que el trigger no se rompa si algún día dejan de ser fijos
 * ("peru"/"lima"/"barranca") en firestorePaths.js.
 */
const syncProductoAlgolia = onDocumentWritten(
  {
    document:
      'Tiendas/{pais}/departamento/{departamento}/provincia/{provincia}/distrito/{localidad}/{subcoleccion}/{tiendaDocId}/categorias/{categoriaId}/productos/{productoId}',
    region: 'us-central1', // REEMPLAZAR si tu proyecto usa otra región
  },
  async (event) => {
    const index = getAlgoliaIndex();
    const { localidad, tiendaDocId, categoriaId, productoId } = event.params;
    const objectID = `${tiendaDocId}_${productoId}`;

    const despues = event.data?.after;
    const existeDespues = despues?.exists;

    // ---- CASO 1: producto eliminado ----
    if (!existeDespues) {
      try {
        await index.deleteObject(objectID);
        console.log(`[syncProductoAlgolia] Eliminado de Algolia: ${objectID}`);
      } catch (err) {
        console.error(`[syncProductoAlgolia] Error eliminando ${objectID}:`, err.message);
      }
      return;
    }

    // ---- CASO 2: producto creado o editado ----
    const data = despues.data();

    let categoriaNombre = '';
    try {
      // 👇 ref RELATIVA: la categoría es el padre de "productos", que es
      // el padre del producto que disparó el evento. No hace falta
      // reconstruir el path a mano, así que da igual cuántos niveles
      // tenga la jerarquía por encima.
      const categoriaSnap = await despues.ref.parent.parent.get();
      categoriaNombre = categoriaSnap.exists ? categoriaSnap.data().nombre : '';
    } catch (err) {
      console.error('[syncProductoAlgolia] Error obteniendo categoría:', err.message);
    }

    const imagenPrincipal = Array.isArray(data.imagenes) && data.imagenes.length
      ? data.imagenes[0].url
      : null;

    const objetoAlgolia = {
      objectID,
      tienda_id: tiendaDocId,
      localidad,
      categoria_id: categoriaId,
      categoria: categoriaNombre,
      nombre: data.nombre || '',
      precio: typeof data.precio === 'number' ? data.precio : 0,
      disponible: !!data.disponible,
      imagen_url: imagenPrincipal,
      // TODO: si agregas descripcion/tags_busqueda/alergenos al dashboard, súmalos aquí también.
    };

    try {
      await index.saveObject(objetoAlgolia);
      console.log(`[syncProductoAlgolia] Sincronizado: ${objectID}`);
    } catch (err) {
      console.error(`[syncProductoAlgolia] Error guardando ${objectID}:`, err.message);
    }
  }
);

/**
 * Si renombras una categoría, propaga el nuevo nombre a todos sus productos
 * ya indexados en Algolia (Algolia no lo hace en cascada solo).
 */
const syncCategoriaAlgolia = onDocumentWritten(
  {
    document:
      'Tiendas/{pais}/departamento/{departamento}/provincia/{provincia}/distrito/{localidad}/{subcoleccion}/{tiendaDocId}/categorias/{categoriaId}',
    region: 'us-central1',
  },
  async (event) => {
    const despues = event.data?.after;
    if (!despues?.exists) return; // el borrado en cascada ya lo maneja el dashboard

    const { tiendaDocId } = event.params;
    const nuevoNombre = despues.data().nombre;

    // 👇 ref RELATIVA otra vez: "productos" es una subcolección directa
    // del documento de categoría que acaba de escribirse.
    const productosSnap = await despues.ref.collection('productos').get();

    if (productosSnap.empty) return;

    const index = getAlgoliaIndex();
    const actualizaciones = productosSnap.docs.map((docSnap) => ({
      objectID: `${tiendaDocId}_${docSnap.id}`,
      categoria: nuevoNombre,
    }));

    try {
      await index.partialUpdateObjects(actualizaciones, { createIfNotExists: false });
      console.log(`[syncCategoriaAlgolia] ${actualizaciones.length} productos actualizados con categoría "${nuevoNombre}"`);
    } catch (err) {
      console.error('[syncCategoriaAlgolia] Error actualizando productos:', err.message);
    }
  }
);

module.exports = { syncProductoAlgolia, syncCategoriaAlgolia };