package it.bosler.numeracy

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform