package it.bosler.numeracy.persistence

import java.io.File

actual class FileStorage actual constructor() {
    private val dir = File(System.getProperty("user.home"), ".numeracy").also { it.mkdirs() }

    actual fun read(fileName: String): String? {
        val file = File(dir, fileName)
        return if (file.exists()) file.readText() else null
    }

    actual fun write(fileName: String, content: String) {
        File(dir, fileName).writeText(content)
    }
}
