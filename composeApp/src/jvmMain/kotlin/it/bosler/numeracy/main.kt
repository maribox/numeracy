package it.bosler.numeracy

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.persistence.FileStorage

fun main() {
    AppContext.initialize(FileStorage())
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Numeracy",
        ) {
            App()
        }
    }
}
