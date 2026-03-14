package it.bosler.numeracy.persistence

object AppContext {
    lateinit var runRepository: RunRepository

    fun initialize(storage: FileStorage) {
        runRepository = RunRepository(storage)
    }
}
