package it.bosler.numeracy

import androidx.compose.ui.window.ComposeUIViewController
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.persistence.FileStorage

fun MainViewController() = run {
    AppContext.initialize(FileStorage())
    ComposeUIViewController { App() }
}
