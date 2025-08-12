package com.geinzz.geinzwork.ui.adapters.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.nosotros_geinz_work
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme

class quienes_somos_geinz_work : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeinzWorkTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    nosotros_geinz_work(innerPadding)
                }
            }
        }
    }
}