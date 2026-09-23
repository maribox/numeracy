package it.bosler.numeracy.persistence

/** The platform's own place for app files: a directory on Android and desktop, localStorage on the web. */
expect class FileStorage() : Storage {
    override fun read(fileName: String): String?
    override fun write(fileName: String, content: String)
}
