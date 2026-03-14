package it.bosler.numeracy.util

actual fun currentTimeMillis(): Long = js("Date.now()").toString().toLong()
