package com.geinzz.geinzwork.model

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavController

val LocalNavController = compositionLocalOf<NavController> {
    error("NavController no ha sido provisto. Envuelve tu jerarquía con LocalNavController.provides(...)")
}