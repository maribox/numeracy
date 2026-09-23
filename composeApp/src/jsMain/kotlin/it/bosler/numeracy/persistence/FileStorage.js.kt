package it.bosler.numeracy.persistence

import kotlinx.browser.localStorage

actual class FileStorage actual constructor() : Storage {
    actual override fun read(fileName: String): String? {
        return localStorage.getItem(fileName)
    }

    actual override fun write(fileName: String, content: String) {
        localStorage.setItem(fileName, content)
    }
}
