package com.trivada.dairyminds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun ScreenStatusBar(userPreferenceColor: Color) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = false

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = userPreferenceColor,
            darkIcons = useDarkIcons
        )
    }
}