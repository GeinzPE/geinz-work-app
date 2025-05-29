package com.example.geinzwork.vistas_p

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.geinzwork.adapterViewholder.adapter_onboarding_como_usa
import com.example.geinzwork.dataclass.dataclass_onboarding_comousar
import com.example.geinzwork.vistas_principales.onboarding_activitys.onboardingAdapterVerificado
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityOnboardingComoUsarGeinzBinding

class onboarding_como_usar_geinz : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingComoUsarGeinzBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingComoUsarGeinzBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val lista = listOf(
            dataclass_onboarding_comousar(R.layout.include_como_usar),
            dataclass_onboarding_comousar(R.layout.include_inicio_info),
            dataclass_onboarding_comousar(R.layout.include_noticias_info),
            dataclass_onboarding_comousar(R.layout.include_categorias_info)
        )
        binding.viewPager.adapter = adapter_onboarding_como_usa(lista)

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem + 1 < lista.size) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == lista.size - 1) {
                    binding.btnNext.isVisible = true
                } else {
                    binding.btnNext.isVisible = false

                }
            }
        })


    }

    private fun finishOnboarding() {
        onBackPressed()
    }
}