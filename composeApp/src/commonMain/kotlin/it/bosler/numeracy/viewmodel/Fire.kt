package it.bosler.numeracy.viewmodel

/**
 * The game-mode fire bar: a heat between 0 and 1 that answers raise and time lets fall. It is a
 * value at a moment rather than a number something keeps lowering, so nothing runs while the screen
 * is not drawn, and the bar reads the same whenever and however often it is asked.
 */
data class Fire(
    /** The heat at [since]. */
    val level: Float = 0f,
    /** When [level] was set, on the view model's clock. */
    val since: Long = 0L,
) {
    /** The heat at [now] for a scenario whose median answer takes [medianMs]. */
    fun levelAt(now: Long, medianMs: Long): Float = decayed(level, now - since, medianMs)
}

/** How often the heat drops, in ms. The drop per step is what the curve was tuned with. */
internal const val FIRE_STEP_MS = 200L

/**
 * [level] after [elapsedMs] of cooling. The fire drains fully in about twice the scenario's median
 * answer time, fastest while hot: each step removes level² · k plus a small constant, and anything
 * under 1% goes out.
 */
internal fun decayed(level: Float, elapsedMs: Long, medianMs: Long): Float {
    val steps = medianMs * 2f / FIRE_STEP_MS
    val k = 3f / steps
    val c = k * 0.06f
    var heat = level
    var remaining = elapsedMs / FIRE_STEP_MS
    while (remaining > 0 && heat > 0f) {
        heat = (heat - (heat * heat * k + c)).coerceAtLeast(0f)
        if (heat < 0.01f) heat = 0f
        remaining--
    }
    return heat
}

/** How much a right answer adds to the heat, by how its time compares to the scenario's median. */
internal fun heatFor(elapsedMs: Long, medianMs: Long): Float = when {
    elapsedMs < medianMs * 0.3 -> 0.75f
    elapsedMs < medianMs * 0.6 -> 0.55f
    elapsedMs < medianMs * 1.2 -> 0.35f
    else -> 0.18f
}

/** What a wrong answer takes off. */
internal const val WRONG_ANSWER_COOLING = 0.3f

/** Points for a right answer at [heat]: fifty, multiplied by one to six as the fire grows. */
internal fun pointsFor(heat: Float): Int = (50 * (1.0 + heat * 5.0)).toInt()
