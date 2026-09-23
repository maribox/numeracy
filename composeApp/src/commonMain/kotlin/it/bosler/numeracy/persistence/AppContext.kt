package it.bosler.numeracy.persistence

/** Where the app's one repository lives, set up by each platform's entry point before the first screen. */
object AppContext {
    private var repository: RunRepository? = null

    val runRepository: RunRepository
        get() = checkNotNull(repository) { "AppContext.initialize has not been called by the platform entry point" }

    fun initialize(storage: Storage) {
        repository = RunRepository(storage)
    }
}
