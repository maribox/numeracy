package it.bosler.numeracy

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.persistence.FileStorage

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    AppContext.initialize(FileStorage())
    ComposeViewport {
        App()
    }
}
