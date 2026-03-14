package it.bosler.numeracy.persistence

expect class FileStorage() {
    fun read(fileName: String): String?
    fun write(fileName: String, content: String)
}
