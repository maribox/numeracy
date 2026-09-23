package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

/** How many questions each scenario is asked for, per difficulty and seed. */
internal const val SAMPLES_PER_SEED = 400

internal val SEEDS = listOf(1, 2, 3, 20260728)

/** One generated question, with where it came from so a failure can be reproduced. */
internal data class Sample(
    val type: ScenarioType,
    val difficulty: Difficulty,
    val seed: Int,
    val index: Int,
    val problem: Problem,
) {
    override fun toString() = "$type/$difficulty seed=$seed #$index: ${problem.questionText.replace("\n", " ")} -> ${problem.correctAnswer}"
}

/** Every question the generators ask across every scenario, difficulty and seed. */
internal fun everySample(types: Collection<ScenarioType> = ScenarioType.entries): Sequence<Sample> = sequence {
    for (type in types) for (difficulty in type.availableDifficulties) for (seed in SEEDS) {
        val generator = generatorFor(type, difficulty, Random(seed))
        repeat(SAMPLES_PER_SEED) { index -> yield(Sample(type, difficulty, seed, index, generator.generate())) }
    }
}

internal fun samplesOf(vararg types: ScenarioType) = everySample(types.toList())

/** Collects every violation instead of stopping at the first, so one run shows the whole shape of a bug. */
internal class Violations(private val what: String) {
    private val found = mutableListOf<String>()
    fun check(condition: Boolean, sample: Any, detail: () -> String) {
        if (!condition) found += "${detail()}  [$sample]"
    }
    fun assertNone() {
        if (found.isNotEmpty()) {
            throw AssertionError(
                "$what: ${found.size} violations, first ${minOf(12, found.size)}:\n" +
                    found.take(12).joinToString("\n") { "  - $it" }
            )
        }
    }
}
