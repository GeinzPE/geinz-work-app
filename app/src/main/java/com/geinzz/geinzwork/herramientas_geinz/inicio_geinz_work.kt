package com.geinzz.geinzwork.herramientas_geinz

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityInicioGeinzWorkBinding

class inicio_geinz_work : AppCompatActivity() {
    private lateinit var binding: ActivityInicioGeinzWorkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioGeinzWorkBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseSecundario.inicializar(this)
        obtener_modelo_celulares_geinz_work()
        val modelos = mutableListOf<ModeloTelefono>()
        // --- 1. Modelos Samsung ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "Samsung",
//                series = "Galaxy A",
//                codigos_internos = listOf(
//                    "SM-A556B",
//                    "SM-A057F",
//                    "SM-A155F"
//                ) // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Samsung",
//                series = "Galaxy S",
//                codigos_internos = listOf(
//                    "SM-S928B",
//                    "SM-S921U",
//                    "SM-S926N"
//                ) // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Samsung",
//                series = "Galaxy Z",
//                codigos_internos = listOf("SM-F946B", "SM-F731U") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Samsung",
//                series = "Galaxy J",
//                codigos_internos = listOf("SM-J730F", "SM-J530G") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Samsung",
//                series = "Galaxy M",
//                codigos_internos = listOf("SM-M546B", "SM-M346B") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Samsung",
//                series = "Galaxy Note",
//                codigos_internos = listOf("SM-N985F", "SM-N970U") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Samsung",
//                series = "Galaxy F",
//                codigos_internos = listOf("SM-E546B", "SM-E146B") // Ejemplo representativo
//            )
//        )
//
//        // --- 2. Modelos Xiaomi ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "Xiaomi",
//                series = "Xiaomi (Serie Principal)",
//                codigos_internos = listOf("2403DPARA", "23013PC75G") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Xiaomi",
//                series = "Redmi Note",
//                codigos_internos = listOf("23090RA98G", "2211131G") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Xiaomi",
//                series = "Redmi (Serie Básica)",
//                codigos_internos = listOf("23124RN87G", "23053RN02L") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Xiaomi",
//                series = "POCO F",
//                codigos_internos = listOf("2406FPB", "23013PC75G") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Xiaomi",
//                series = "POCO X",
//                codigos_internos = listOf("2312FPCA6G", "22101320G") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Xiaomi",
//                series = "POCO M",
//                codigos_internos = listOf("2312FPCA4G", "22071219CG") // Ejemplo representativo
//            )
//        )
//
//        // --- 3. Modelos Motorola ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "Motorola",
//                series = "Moto G",
//                codigos_internos = listOf("XT2347-1", "XT2417-1") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Motorola",
//                series = "Motorola Edge",
//                codigos_internos = listOf("XT2347-4", "XT2401-2") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Motorola",
//                series = "Moto E",
//                codigos_internos = listOf("XT2345-1", "XT2155-1") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Motorola",
//                series = "Motorola Razr",
//                codigos_internos = listOf("XT2321-1", "XT2323-1") // Ejemplo representativo
//            )
//        )
//
//        // --- 4. Modelos Apple ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "Apple",
//                series = "iPhone",
//                codigos_internos = listOf("A2849", "A2649", "A2595") // Ejemplo representativo
//            )
//        )
//
//        // --- 5. Modelos Realme ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "Realme",
//                series = "Realme Number",
//                codigos_internos = listOf("RMX3840", "RMX3740") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Realme",
//                series = "Realme C",
//                codigos_internos = listOf("RMX3890", "RMX3710") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Realme",
//                series = "Realme GT",
//                codigos_internos = listOf("RMX3888", "RMX3850") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Realme",
//                series = "Realme Narzo",
//                codigos_internos = listOf("RMX3863", "RMX3630") // Ejemplo representativo
//            )
//        )
//
//        // --- 6. Modelos HONOR ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "HONOR",
//                series = "HONOR X",
//                codigos_internos = listOf("CRT-LX1", "ALI-NX1") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "HONOR",
//                series = "HONOR Magic",
//                codigos_internos = listOf("BVL-AN16", "VER-AN10") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "HONOR",
//                series = "HONOR N",
//                codigos_internos = listOf("REA-NX9", "FNE-NX9") // Ejemplo representativo
//            )
//        )
//
//        // --- 7. Modelos OPPO ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "OPPO",
//                series = "OPPO Reno",
//                codigos_internos = listOf("CPH2581", "CPH2521") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "OPPO",
//                series = "OPPO A",
//                codigos_internos = listOf("CPH2631", "CPH2579") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "OPPO",
//                series = "OPPO Find X",
//                codigos_internos = listOf("PHZ110", "PGEM10") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "OPPO",
//                series = "OPPO Find N",
//                codigos_internos = listOf("CPH2519", "PCL110") // Ejemplo representativo
//            )
//        )
//
//        // --- 8. Modelos Tecno ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "Tecno",
//                series = "Tecno Spark",
//                codigos_internos = listOf("KJ7", "KI7") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Tecno",
//                series = "Tecno Camon",
//                codigos_internos = listOf("CL9", "CK8n") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Tecno",
//                series = "Tecno Pova",
//                codigos_internos = listOf("LI9", "LH7n") // Ejemplo representativo
//            )
//        )
//
//        // --- 9. Modelos Infinix ---
//        modelos.add(
//            ModeloTelefono(
//                marca = "Infinix",
//                series = "Infinix Note",
//                codigos_internos = listOf("X6880", "X6710") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Infinix",
//                series = "Infinix Smart",
//                codigos_internos = listOf("X6525", "X6515") // Ejemplo representativo
//            )
//        )
//        modelos.add(
//            ModeloTelefono(
//                marca = "Infinix",
//                series = "Infinix Hot",
//                codigos_internos = listOf("X6835", "X6721") // Ejemplo representativo
//            )
//        )
        binding.agregar.setOnClickListener {
            agregar_coleciones_marca(binding.modeloBuscar.text.toString())
        }
    }

    // Data class para CADA SERIE (el "mapa" dentro del array)
    data class SerieEnArrayFirestore(
        var ejemplos_clave: String = "", // e.g., "'a03core,a20s,a51,...'"
        var nombre: String = ""         // e.g., "Galaxy A"
    )

    // Helper function to format internal codes into the 'ejemplos_clave' string
    fun formatCodigosParaEjemplo(codigos: List<String>): String {
        return "'${codigos.joinToString(",")}'" // Une los códigos con comas y los encierra en comillas simples
    }

    // Función para obtener los datos de las series para una marca específica
// Retorna una lista de objetos SerieEnArrayFirestore
    fun getSeriesDataForBrandAsObjects(brandName: String): List<SerieEnArrayFirestore> {
        val seriesList = mutableListOf<SerieEnArrayFirestore>()

        when (brandName) {
            "Samsung" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy A", ejemplos_clave = formatCodigosParaEjemplo(listOf("A03 core", "A04e", "A05", "A10", "A10s", "A11", "A12", "A13", "A14", "A15", "A20", "A20s", "A21s", "A22", "A23", "A24", "A25", "A30", "A30s", "A31", "A32", "A33", "A34", "A35", "A50", "A50s", "A51", "A52", "A52s", "A53", "A54", "A55", "A70", "A71", "A72", "A73", "A80", "A90"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy S", ejemplos_clave = formatCodigosParaEjemplo(listOf("S24 Ultra", "S24+", "S24", "S23 Ultra", "S23+", "S23", "S22 Ultra", "S22+", "S22", "S21 Ultra", "S21+", "S21", "S20 Ultra", "S20+", "S20", "S10", "S10e", "S10+", "S9", "S9+"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy Z", ejemplos_clave = formatCodigosParaEjemplo(listOf("Z Fold5", "Z Flip5", "Z Fold4", "Z Flip4", "Z Fold3", "Z Flip3", "Z Fold2", "Z Flip"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy J", ejemplos_clave = formatCodigosParaEjemplo(listOf("J1", "J2", "J2 Prime", "J2 Core", "J3", "J4", "J4+", "J5", "J5 Prime", "J6", "J6+", "J7", "J7 Prime", "J7 Pro", "J8"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy M", ejemplos_clave = formatCodigosParaEjemplo(listOf("M04", "M10", "M11", "M12", "M13", "M14", "M20", "M21", "M22", "M23", "M30", "M30s", "M31", "M31s", "M32", "M33", "M34", "M40", "M42", "M44", "M51", "M52", "M53", "M54", "M55", "M62"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy Note", ejemplos_clave = formatCodigosParaEjemplo(listOf("Note 20 Ultra", "Note 20", "Note 10+", "Note 10", "Note 9", "Note 8"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy F", ejemplos_clave = formatCodigosParaEjemplo(listOf("F12", "F13", "F14", "F22", "F23", "F34", "F41", "F42", "F54", "F55", "F62")))
                )
                seriesList.add(SerieEnArrayFirestore(nombre = "Galaxy E", ejemplos_clave = formatCodigosParaEjemplo(listOf("E5", "E7", "E22i")))) // Agregado E22i
            }
            "Xiaomi" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "Xiaomi (Serie Principal)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Xiaomi 14 Ultra", "Xiaomi 14", "Xiaomi 13 Ultra", "Xiaomi 13 Pro", "Xiaomi 13", "Xiaomi 12S Ultra", "Xiaomi 12 Pro", "Xiaomi 12", "Xiaomi 11 Ultra", "Mi 11", "Mi 10", "Mi 9", "Mi 8"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Redmi Note", ejemplos_clave = formatCodigosParaEjemplo(listOf("Redmi Note 13 Pro+", "Redmi Note 13 Pro", "Redmi Note 13", "Redmi Note 12 Pro+", "Redmi Note 12 Pro", "Redmi Note 12", "Redmi Note 11 Pro+", "Redmi Note 11 Pro", "Redmi Note 11", "Redmi Note 10 Pro", "Redmi Note 10", "Redmi Note 9 Pro", "Redmi Note 9"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Redmi (Serie Básica)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Redmi 13C", "Redmi 12C", "Redmi 10A", "Redmi 9A", "Redmi 9C", "Redmi 9", "Redmi 8A", "Redmi 8"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "POCO F", ejemplos_clave = formatCodigosParaEjemplo(listOf("POCO F6 Pro", "POCO F6", "POCO F5 Pro", "POCO F5", "POCO F4 GT", "POCO F3"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "POCO X", ejemplos_clave = formatCodigosParaEjemplo(listOf("POCO X6 Pro", "POCO X6", "POCO X5 Pro", "POCO X5", "POCO X4 Pro", "POCO X3 Pro", "POCO X3 NFC"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "POCO M", ejemplos_clave = formatCodigosParaEjemplo(listOf("POCO M6 Pro", "POCO M6", "POCO M5", "POCO M4 Pro", "POCO M3"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Redmi K", ejemplos_clave = formatCodigosParaEjemplo(listOf("Redmi K70 Pro", "Redmi K70", "Redmi K60 Ultra", "Redmi K60 Pro", "Redmi K60"))))
            }
            "Motorola" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "Moto G", ejemplos_clave = formatCodigosParaEjemplo(listOf("Moto G Stylus (2024)", "Moto G Power 5G (2024)", "Moto G Play (2024)", "Moto G84", "Moto G73", "Moto G54", "Moto G42", "Moto G32", "Moto G22", "Moto G10", "Moto G9", "Moto G8"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Motorola Edge", ejemplos_clave = formatCodigosParaEjemplo(listOf("Edge 50 Ultra", "Edge 50 Pro", "Edge 50 Fusion", "Edge 40 Pro", "Edge 40 Neo", "Edge 30 Ultra", "Edge 30 Fusion", "Edge 20 Pro", "Edge+"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Moto E", ejemplos_clave = formatCodigosParaEjemplo(listOf("Moto E13", "Moto E22", "Moto E20", "Moto E7", "Moto E6i"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Motorola Razr", ejemplos_clave = formatCodigosParaEjemplo(listOf("Razr 40 Ultra", "Razr 40", "Razr 2022", "Razr 5G", "Razr (2019)"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Motorola One", ejemplos_clave = formatCodigosParaEjemplo(listOf("One Fusion", "One Fusion+", "One Hyper", "One Action", "One Vision", "One Macro", "One Power")))) // Agregado Motorola One Fusion
                seriesList.add(SerieEnArrayFirestore(nombre = "Moto X", ejemplos_clave = formatCodigosParaEjemplo(listOf("Moto X4", "Moto X Style", "Moto X Pure Edition")))) // Menos comunes ahora
                seriesList.add(SerieEnArrayFirestore(nombre = "Moto Z", ejemplos_clave = formatCodigosParaEjemplo(listOf("Moto Z4", "Moto Z3 Play", "Moto Z2 Force", "Moto Z Play")))) // Menos comunes ahora
            }
            "Apple" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "iPhone", ejemplos_clave = formatCodigosParaEjemplo(listOf("iPhone 15 Pro Max", "iPhone 15 Pro", "iPhone 15 Plus", "iPhone 15", "iPhone 14 Pro Max", "iPhone 14 Pro", "iPhone 14 Plus", "iPhone 14", "iPhone 13 Pro Max", "iPhone 13 Pro", "iPhone 13 mini", "iPhone 13", "iPhone SE (2022)", "iPhone 12 Pro Max", "iPhone 12 Pro", "iPhone 12 mini", "iPhone 12", "iPhone 11 Pro Max", "iPhone 11 Pro", "iPhone 11", "iPhone XR", "iPhone XS Max", "iPhone XS", "iPhone X", "iPhone 8", "iPhone 7"))))
            }
            "Realme" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "Realme Number (Serie Principal)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Realme 12 Pro+", "Realme 12 Pro", "Realme 12", "Realme 11 Pro+", "Realme 11 Pro", "Realme 11", "Realme 10 Pro+", "Realme 10 Pro", "Realme 10", "Realme 9 Pro+", "Realme 9 Pro", "Realme 9"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Realme C (Serie C)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Realme C67", "Realme C55", "Realme C35", "Realme C33", "Realme C30s", "Realme C25s", "Realme C21Y"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Realme GT (Serie Flagship)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Realme GT 5 Pro", "Realme GT 5", "Realme GT Neo5 SE", "Realme GT Neo5", "Realme GT Neo3", "Realme GT2 Pro", "Realme GT Master Edition"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Realme Narzo", ejemplos_clave = formatCodigosParaEjemplo(listOf("Narzo 70 Pro 5G", "Narzo 60 Pro 5G", "Narzo 50 Pro 5G", "Narzo 50A Prime", "Narzo 50"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Realme X (Antigua Flagship)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Realme X7 Pro", "Realme X50 Pro", "Realme XT")))) // Aunque menos recientes, eran importantes
            }
            "HONOR" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "HONOR X (Gama Media)", ejemplos_clave = formatCodigosParaEjemplo(listOf("HONOR X9b", "HONOR X8b", "HONOR X7b", "HONOR X6a", "HONOR X5 Plus", "HONOR X9a", "HONOR X8a", "HONOR X7a", "HONOR X6", "HONOR X9", "HONOR X8", "HONOR X7"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "HONOR Magic (Gama Alta)", ejemplos_clave = formatCodigosParaEjemplo(listOf("HONOR Magic6 Pro", "HONOR Magic6 Lite", "HONOR Magic V2", "HONOR Magic5 Pro", "HONOR Magic5 Lite", "HONOR Magic4 Pro", "HONOR Magic V"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "HONOR N (Serie Numérica)", ejemplos_clave = formatCodigosParaEjemplo(listOf("HONOR 200 Pro", "HONOR 200 Lite", "HONOR 90", "HONOR 70", "HONOR 50"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "HONOR Play", ejemplos_clave = formatCodigosParaEjemplo(listOf("HONOR Play 8T", "HONOR Play 7T", "HONOR Play 6C"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "HONOR V (Antigua Flagship)", ejemplos_clave = formatCodigosParaEjemplo(listOf("HONOR V40", "HONOR V30 Pro"))))
            }
            "OPPO" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "OPPO Reno", ejemplos_clave = formatCodigosParaEjemplo(listOf("Reno11 F 5G", "Reno11 Pro 5G", "Reno11 5G", "Reno10 Pro+", "Reno10 Pro", "Reno10", "Reno9", "Reno8 T", "Reno7", "Reno6"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "OPPO A (Gama Media/Entrada)", ejemplos_clave = formatCodigosParaEjemplo(listOf("OPPO A60", "OPPO A38", "OPPO A58", "OPPO A79", "OPPO A98", "OPPO A78", "OPPO A57", "OPPO A17", "OPPO A16"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "OPPO Find X (Flagship)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Find X7 Ultra", "Find X7", "Find X6 Pro", "Find X6", "Find X5 Pro", "Find X5", "Find X3 Pro", "Find X2 Pro"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "OPPO Find N (Plegables)", ejemplos_clave = formatCodigosParaEjemplo(listOf("Find N3 Flip", "Find N3", "Find N2 Flip", "Find N2", "Find N"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "OPPO K", ejemplos_clave = formatCodigosParaEjemplo(listOf("OPPO K12", "OPPO K11x", "OPPO K10 Pro", "OPPO K9s"))))
            }
            "Tecno" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "Tecno Spark", ejemplos_clave = formatCodigosParaEjemplo(listOf("Spark 20 Pro+", "Spark 20 Pro", "Spark 20", "Spark 10 Pro", "Spark 10", "Spark 9 Pro", "Spark 8 Pro"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Tecno Camon", ejemplos_clave = formatCodigosParaEjemplo(listOf("Camon 30 Premier", "Camon 30 Pro", "Camon 30", "Camon 20 Premier", "Camon 20 Pro", "Camon 20", "Camon 19 Pro"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Tecno Pova", ejemplos_clave = formatCodigosParaEjemplo(listOf("Pova 6 Pro 5G", "Pova 5 Pro 5G", "Pova 5", "Pova 4 Pro", "Pova 4", "Pova Neo 2"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Tecno Phantom", ejemplos_clave = formatCodigosParaEjemplo(listOf("Phantom V Flip", "Phantom V Fold", "Phantom X2 Pro", "Phantom X2", "Phantom X"))))
            }
            "Infinix" -> {
                seriesList.add(SerieEnArrayFirestore(nombre = "Infinix Note", ejemplos_clave = formatCodigosParaEjemplo(listOf("Note 40 Pro 5G", "Note 40 5G", "Note 40", "Note 30 VIP", "Note 30", "Note 12 Pro 5G", "Note 12", "Note 11 Pro"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Infinix Smart", ejemplos_clave = formatCodigosParaEjemplo(listOf("Smart 8 Pro", "Smart 8", "Smart 7", "Smart 6 Plus", "Smart 5"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Infinix Hot", ejemplos_clave = formatCodigosParaEjemplo(listOf("Hot 40 Pro", "Hot 40i", "Hot 30", "Hot 20 5G", "Hot 20S", "Hot 12 Pro"))))
                seriesList.add(SerieEnArrayFirestore(nombre = "Infinix Zero", ejemplos_clave = formatCodigosParaEjemplo(listOf("Zero 30 5G", "Zero 20", "Zero X Pro", "Zero 5G"))))
            }
            else -> {
                println("Marca '$brandName' no reconocida. No se agregarán series.")
            }
        }
        return seriesList
    }

    // Función principal para agregar la colección de modelos por marca
    private fun agregar_coleciones_marca(marca: String) {
        val db = FirebaseSecundario.getFirestore() // Obtén tu instancia de Firestore

        // Referencia al documento específico de la marca dentro de la subcolección 'modelos'
        // La ruta será: Tenicos/marcas/modelos/{nombre_de_la_marca}
        val marcaDocumentRef =
            db.collection("Tenicos").document("marcas").collection("modelos").document(marca)

        // Obtener la lista de series para la marca dada en el formato de objetos SerieEnArrayFirestore
        val seriesParaEstaMarca = getSeriesDataForBrandAsObjects(marca)

        // Construir el HashMap que representará el documento de la marca
        // Firestore puede convertir directamente List<data class> a un array de mapas.
        val dataToUpload = hashMapOf<String, Any>(
            "nombre" to marca, // El campo 'nombre' en el documento es el nombre de la marca
            "series_disponibles" to seriesParaEstaMarca // ¡Aquí está el ARRAY DE OBJETOS/MAPAS!
        )

        // Subir el HashMap al documento de Firestore
        marcaDocumentRef.set(dataToUpload)
            .addOnSuccessListener {
                println("Documento de marca '$marca' con series agregada con éxito en la ruta: Tenicos/marcas/modelos/$marca")
            }
            .addOnFailureListener { e ->
                System.err.println("Error al agregar el documento de marca '$marca': ${e.message}")
            }
    }


    private fun obtener_modelo_celulares_geinz_work() {
        // 🔹 Obtener la instancia de Firestore desde FirebaseSecundario
        val db = FirebaseSecundario.getFirestore()

        // 🔹 Acceder al documento específico
        db.collection("componentesMaestros")
            .document("PANTALLA_OLED_SAMSUNG_A52_GEN1")
            .get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val data = documento.data
                    Log.d("GeinzWork_obtenos_datos", "Datos: $data")
                    Toast.makeText(this, "Datos: $data", Toast.LENGTH_SHORT).show()


                } else {
                    Log.w("GeinzWork", "El documento no existe.")
                }
            }
            .addOnFailureListener { error ->
                Log.e("GeinzWork", "Error al obtener datos: ", error)
            }
    }


}