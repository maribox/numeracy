package it.bosler.numeracy.persistence

/** Named text files, which is all the app keeps: the practice history and one setting. */
interface Storage {
    fun read(fileName: String): String?

    /** Replaces [fileName] with [content] entirely or not at all. */
    fun write(fileName: String, content: String)
}

/** Storage that lives as long as the object does: for tests, and for drawing screens off-device. */
class InMemoryStorage(initial: Map<String, String> = emptyMap()) : Storage {
    private val files = initial.toMutableMap()
    override fun read(fileName: String): String? = files[fileName]
    override fun write(fileName: String, content: String) {
        files[fileName] = content
    }
}
