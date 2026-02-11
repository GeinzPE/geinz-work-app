package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay


    @Composable
    fun TypewriterText(
        text: String,
        modifier: Modifier = Modifier,
        speed: Long = 50L
    ) {
        var displayText by remember { mutableStateOf("") }

        LaunchedEffect(text) {
            displayText = ""
            for (i in text.indices) {
                displayText += text[i]
                delay(speed)
            }
        }

        Text(
            text = displayText,
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium
        )
    }

