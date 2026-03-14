package it.bosler.numeracy.util

actual fun currentTimeMillis(): Long = kotlin.js.Date.now().toLong()
