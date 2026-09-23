package it.bosler.numeracy.persistence

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

actual class FileStorage actual constructor() : Storage {
    companion object {
        /** The app's private files directory, set by the activity before anything is read. */
        lateinit var filesDir: File
    }

    actual override fun read(fileName: String): String? {
        val file = File(filesDir, fileName)
        return if (file.exists()) file.readText() else null
    }

    /**
     * Writes beside the file and moves it into place, so a process killed mid-write leaves the old
     * file whole rather than half of the new one: a history that no longer parses is a history lost.
     */
    actual override fun write(fileName: String, content: String) {
        val target = File(filesDir, fileName)
        val partial = File(filesDir, "$fileName.partial")
        partial.writeText(content)
        Files.move(partial.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
}
