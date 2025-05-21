package com.example.geinzwork.dataclass

data class dataclass_reporte_denuncia_tb(
    val idtrabajador: String?,
    val idusuario: String?,
    val idreporte: String?,
    val tipoReporte: String?,
    val problema: String?,
    val estado: String?,
    val tipo_enviado_recivido: String?,
    val fechaEnvio: String?,
    val horaEnvio: String?
)
