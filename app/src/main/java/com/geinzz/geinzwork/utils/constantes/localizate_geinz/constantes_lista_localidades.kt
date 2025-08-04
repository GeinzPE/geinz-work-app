package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_localidad_escudos
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.localizate_geinz.dataclass_cat_sub

object constantes_lista_localidades {
    val lista = listOf(
        dataclass_localidad_escudos("Barranca".lowercase(), R.drawable.escudo_barranca),
        dataclass_localidad_escudos("Paramonga".lowercase(), R.drawable.escudo_paramonga),
        dataclass_localidad_escudos("Supe".lowercase(), R.drawable.escudo_supe),
        dataclass_localidad_escudos("Pativilca".lowercase(), R.drawable.escudo_pativilca)
    )
    val dias_sema =
        listOf("lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo")


//    val listaCategorias = listOf(
//        dataclass_cat_sub(
//            "belleza", listOf(
//                "peluquerias",
//                "barberias",
//                "spas",
//                "salones de unas",
//                "centros esteticos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "comida y restaurantes", listOf(
//                "pollerias",
//                "chifas",
//                "pizzerias",
//                "cevicherias",
//                "restaurantes criollos",
//                "comida rapida",
//                "pastelerias",
//                "cafeterias",
//                "heladerias"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "educacion y librerias", listOf(
//                "institutos educativos",
//                "universidades privadas",
//                "colegios privados",
//                "librerias"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "hogar y ferreteria", listOf(
//                "mueblerias",
//                "vidrierias",
//                "ferreterias",
//                "tiendas de decoracion",
//                "tiendas de electrodomesticos",
//                "colchoneras",
//                "tiendas de iluminacion"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "moda y estilo", listOf(
//                "tiendas de ropa",
//                "tiendas de calzado",
//                "ropa deportiva",
//                "accesorios de moda",
//                "boutiques"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "salud y farmacias", listOf(
//                "boticas",
//                "farmacias",
//                "consultorios medicos",
//                "laboratorios clinicos",
//                "opticas",
//                "consultorios dentales"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "mascotas y animales", listOf(
//                "veterinarias",
//                "tiendas para mascotas",
//                "alimentos para mascotas",
//                "accesorios para mascotas",
//                "banos y peluqueria canina"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "supermercados y tiendas grandes", listOf(
//                "supermercados",
//                "mayoristas",
//                "tiendas por departamento",
//                "mercados centrales"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "minimarkets y bodegas", listOf(
//                "minimarkets",
//                "bodegas",
//                "licorerias",
//                "abarrotes",
//                "distribuidoras de agua y gas"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "transporte y terminales", listOf(
//                "paraderos de moto",
//                "paraderos de combi",
//                "terminales terrestres",
//                "agencias de transporte",
//                "cooperativas de transporte"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "viajes y turismo", listOf(
//                "agencias de viaje",
//                "paquetes turisticos",
//                "turismo local",
//                "boletos terrestres y aereos"
//            ), emptyList()
//        ),
//
//        dataclass_cat_sub(
//            "jardineria y plantas", listOf(
//                "viveros",
//                "tiendas de plantas",
//                "tiendas de abonos y fertilizantes",
//                "control de plagas",
//                "productos para jardin"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "grifos y estaciones", listOf(
//                "grifos",
//                "estaciones de servicio",
//                "venta de gasolina",
//                "venta de gas vehicular",
//                "lubricentros"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "bancos y servicios financieros", listOf(
//                "bancos",
//                "cajas municipales",
//                "cooperativas",
//                "casas de cambio",
//                "agencias financieras"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "servicios tecnicos y reparaciones", listOf(
//                "reparacion de celulares",
//                "reparacion de laptops y computadoras",
//                "reparacion de televisores",
//                "reparacion de refrigeradoras",
//                "reparacion de licuadoras",
//                "reparacion de electrodomesticos",
//                "servicios de lavado de electrodomesticos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "mecanica y autoservicios", listOf(
//                "mecanica de motos",
//                "mecanica de autos",
//                "repuestos para motos",
//                "repuestos para autos",
//                "talleres de motos",
//                "talleres de autos",
//                "lavado de vehiculos"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "entretenimiento y recreacion", listOf(
//                "billares",
//                "casas de apuestas",
//                "salas de videojuegos",
//                "cabinas de internet"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "tecnologia y electronica", listOf(
//                "venta de celulares",
//                "venta de accesorios para celulares",
//                "venta de computadoras",
//                "venta de partes y perifericos",
//                "tiendas de electronica menor"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "fotografia e impresion", listOf(
//                "cabinas fotograficas",
//                "fotografias para dni y carnet",
//                "servicios de copias",
//                "plastificados y escaneos",
//                "imprentas"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "deporte y bienestar", listOf(
//                "gimnasios",
//                "centros de yoga",
//                "centros fitness",
//                "centros de pilates"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "utiles y papelerias", listOf(
//                "tiendas de utiles escolares",
//                "papelerias",
//                "venta de materiales de oficina",
//                "venta de cuadernos y lapiceros"
//            ), emptyList()
//        ),
//
//        dataclass_cat_sub(
//            "lavanderias y tintorerias", listOf(
//                "lavanderias",
//                "tintorerias",
//                "lavado en seco",
//                "planchado de ropa"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "servicios de encomienda y envios", listOf(
//                "agencias de encomienda",
//                "courier local",
//                "envios nacionales",
//                "servicios de delivery",
//                "paqueteria"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "funerarias", listOf(
//                "servicios funerarios",
//                "velatorios",
//                "venta de ataudes",
//                "traslados funerarios",
//                "cremacion"
//            ), emptyList()
//        ),
//        dataclass_cat_sub(
//            "hospedaje y entretenimiento nocturno", listOf(
//                "hoteles",
//                "hostales",
//                "hospedajes",
//                "discotecas",
//                "bares",
//                "karaokes",
//                "salones de eventos",
//                "moteles"
//            ), emptyList()
//        )
//    )
}