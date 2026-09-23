package it.bosler.numeracy.persistence

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

actual class FileStorage actual constructor() : Storage {
    private val dir = File(System.getProperty("user.home"), ".numeracy").also { it.mkdirs() }

    actual override fun read(fileName: String): String? {
        val file = File(dir, fileName)
        return if (file.exists()) file.readText() else null
    }

    /** Writes beside the file and moves it into place, so an interrupted write leaves the old file whole. */
    actual override fun write(fileName: String, content: String) {
        val target = File(dir, fileName)
        val partial = File(dir, "$fileName.partial")
        partial.writeText(content)
        Files.move(partial.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
}
