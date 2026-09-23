package it.bosler.numeracy.util

/** Date.now() in ms. Kotlin/Wasm allows js() only as the whole body of a top-level function. */
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun dateNow(): Double = js("Date.now()")

actual fun currentTimeMillis(): Long = dateNow().toLong()
