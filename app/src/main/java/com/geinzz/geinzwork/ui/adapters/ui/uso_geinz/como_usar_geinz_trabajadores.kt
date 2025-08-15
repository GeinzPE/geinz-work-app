package com.geinzz.geinzwork.ui.adapters.ui.uso_geinz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.geinzz.geinzwork.ui.adapters.ui.ui.theme.GeinzWorkTheme

class como_usar_geinz_trabajadores : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeinzWorkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PagerConBotones(innerPadding)
                }
            }
        }
    }
}
