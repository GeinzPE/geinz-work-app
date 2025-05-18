package com.example.geinzwork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.geinzwork.adapterViewholder.adaoter_onboarding
import com.example.geinzwork.dataclass.dataclass_onboarding
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Verifica si ya vio el onboarding
        if (isOnboardingFinished()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }


        val items = listOf(
            dataclass_onboarding(
                "¡Bienvenido a Geinz Work!",
                "Gracias por unirte a nuestra comunidad. Estamos aquí para ayudarte a conectar, comprar y crecer.",
                R.raw.corazon_gracias_animation
            ),
            dataclass_onboarding(
                "Conecta con trabajadores de tu localidad",
                "Contamos con profesionales en Barranca, Supe, Pativilca y Paramonga",
                R.raw.trabajadores_localidad_animation
            ),
            dataclass_onboarding(
                "Encuentra negocios de tu localidad",
                "Descubre tiendas y negocios cercanos. Tenemos establecimientos registrados en Barranca, Supe, Pativilca y Paramonga.",
                R.raw.tiendas_geinz_animation
            ),

            dataclass_onboarding(
                "Compra porductos en Geinz work",
                "Adquiere fácilmente lo que necesitas en tiendas cercanas. Ofertas, promociones y más te esperan.",
                R.raw.carrito_animacion
            ),
            dataclass_onboarding(
                "Registrate gratis en Geinz work",
                "Crea tu cuenta como usuario o trabajador y empieza a disfrutar de todos los beneficios .",
                R.raw.user_registre_animation
            )
        )

        binding.viewPager.adapter = adaoter_onboarding(items)

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < items.size) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        val sharedPref = getSharedPreferences("onboarding", MODE_PRIVATE)
        sharedPref.edit().putBoolean("finished", true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun isOnboardingFinished(): Boolean {
        val sharedPref = getSharedPreferences("onboarding", MODE_PRIVATE)
        return sharedPref.getBoolean("finished", false)
    }
}