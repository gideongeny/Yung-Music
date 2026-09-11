package com.gideongeng.music.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource

fun main() {
    application {
        val windowState = rememberWindowState(size = DpSize(1200.dp, 750.dp))
        Window(
            onCloseRequest = {
                AudioPlayer.release()
                exitApplication()
            },
            title = "YungMusic Desktop",
            state = windowState,
            icon = painterResource("icon.png"),
        ) {
            App()
        }
    }
}
