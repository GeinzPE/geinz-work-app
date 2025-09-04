package com.geinzz.geinzwork

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.geinzz.geinzwork.ui.adapters.ui.loadings.OnboardingPrincipal
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme

class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (isOnboardingFinished()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }


        setContent {
            GeinzWorkTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        OnboardingPrincipal(
                            onFinish = { finishOnboarding() }
                        )
                    }
                }
            }
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
