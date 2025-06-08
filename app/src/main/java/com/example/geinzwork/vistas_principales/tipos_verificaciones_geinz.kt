package com.example.geinzwork.vistas_principales

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.geinzwork.vistas_principales.dataclass_onboarding.onboarding_verificado
import com.example.geinzwork.vistas_principales.onboarding_activitys.onboardingAdapterVerificado
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityTiposVerificacionesGeinzBinding

class tipos_verificaciones_geinz : AppCompatActivity() {
    private lateinit var binding: ActivityTiposVerificacionesGeinzBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTiposVerificacionesGeinzBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val items = listOf(
            onboarding_verificado(
                "Mira los tipos de verificados en Geinz work",
                "En Geinz Work, cada tipo de verificación representa un nivel de confianza y autenticidad. Conoce qué significa cada insignia y cómo beneficia tanto a usuarios como a trabajadores verificados",
                R.raw.verificado_animation,
                null
            ),
            onboarding_verificado(
                "Verificación Verde – Confianza Básica Acreditada",
                "La verificación verde, también conocida como insignia verde, representa el primer nivel dentro de la jerarquía de verificaciones en Geinz Work. Esta verificación indica que el trabajador ha sido revisado y aprobado por el equipo de Geinz Work como una persona confiable y auténtica.\n" +
                        "\n" +
                        "Aunque se trata de una verificación de nivel básico, sigue siendo una garantía de seguridad para los usuarios que interactúan con trabajadores verificados. La diferencia principal entre los distintos niveles de verificación no radica en la validez del perfil, sino en los apartados y funciones adicionales que se habilitan para cada tipo de verificado.",
                null,
                R.drawable.verificado_a
            ),
            onboarding_verificado(
                "Verificación Azul – Mayor confianza y profesionalismo",
                "La verificación azul representa un nivel superior dentro de las insignias de Geinz Work. Esta insignia identifica a trabajadores que demuestran un compromiso serio y confiable en los servicios que ofrecen a los usuarios.\n" +
                        "\n" +
                        "Además, cuenta con beneficios adicionales dentro de la plataforma que mejoran la gestión y visibilidad de su cuenta, facilitando su experiencia en Geinz Work.\n" +
                        "\n" +
                        "Esta distinción ayuda a los usuarios a reconocer rápidamente a profesionales con un mayor nivel de calidad y responsabilidad en sus trabajos.",
                null,
                R.drawable.icon_verificado
            ), onboarding_verificado(
                "Verificación Dorada – Máximo nivel de confianza y prestigio",
                "La verificación dorada, también conocida como Verificación C, representa el nivel más alto de reconocimiento dentro de Geinz Work. Esta insignia distingue a los trabajadores que han pasado por filtros más rigurosos, basados en referencias laborales, historial de servicios y reputación dentro de la plataforma.\n" +
                        "\n" +
                        "Los usuarios con verificación dorada cuentan con un sistema robusto para interactuar con su audiencia y seguidores, además de acceder a funciones avanzadas que potencian su perfil profesional.\n" +
                        "\n" +
                        "Esta verificación transmite el más alto grado de confianza y respaldo dentro de Geinz Work.",
                null,
                R.drawable.verificado_c
            )
        )

        binding.viewPager.adapter = onboardingAdapterVerificado(items)

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < items.size) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == items.size - 1) {
                    binding.btnNext.isVisible = true
                } else {
                    binding.btnNext.isVisible = false

                }
            }
        })


    }

    private fun finishOnboarding() {
        val sharedPref = getSharedPreferences("onboarding", MODE_PRIVATE)
        sharedPref.edit().putBoolean("finished", true).apply()
        onBackPressed()
    }
}