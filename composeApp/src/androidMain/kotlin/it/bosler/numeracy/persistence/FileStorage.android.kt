package it.bosler.numeracy.persistence

import java.io.File

actual class FileStorage actual constructor() {
    companion object {
        lateinit var filesDir: File
    }

    actual fun read(fileName: String): String? {
        val file = File(filesDir, fileName)
        return if (file.exists()) file.readText() else null
    }

    actual fun write(fileName: String, content: String) {
        File(filesDir, fileName).writeText(content)
    }
}
