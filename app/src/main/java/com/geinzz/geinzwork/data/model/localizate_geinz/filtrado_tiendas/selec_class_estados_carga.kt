package com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas

sealed class selec_class_estados_carga {
 object sin_carga: selec_class_estados_carga()
 object carga_principal: selec_class_estados_carga()
 object carga_chips: selec_class_estados_carga()
 object carga_todos: selec_class_estados_carga()
}
