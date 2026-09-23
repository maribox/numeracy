package it.bosler.numeracy.generator

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The trick lists, for every number the scenarios can ask about rather than a sample of them: a
 * pair that matches no trick crashes the practice screen, so none may be left to chance.
 */
class TrickCoverageTest {

    private fun endsAt(trick: MathTrick, answer: Long): Boolean {
        val last = trick.steps.last().replace(Regex("#[a-z]\\{|}"), "")
        return Regex("\\d[\\d,]*").findAll(last).any { it.value.replace(",", "") == answer.toString() }
    }

    @Test
    fun everyProductHasATrickAndEveryTrickEndsAtTheProduct() {
        val generator = MultiplicationGenerator()
        val problems = mutableListOf<String>()
        for (a in 10..99) for (b in 10..99) {
            val answer = a.toLong() * b
            val tricks = runCatching { generator.buildApplicableTricks(a, b, answer) }.getOrElse { emptyList() }
            if (tricks.isEmpty()) problems += "$a × $b has no trick"
            tricks.filterNot { endsAt(it, answer) }.forEach { problems += "$a × $b: '${it.name}' ends at '${it.steps.last()}'" }
        }
        assertTrue(problems.isEmpty(), "${problems.size} problems, first: ${problems.take(10)}")
    }

    @Test
    fun everySquareHasATrickAndEveryTrickEndsAtTheSquare() {
        val generator = SquaringGenerator()
        val problems = mutableListOf<String>()
        for (n in 10..99) {
            val answer = n.toLong() * n
            val tricks = runCatching { generator.buildApplicableTricks(n, answer) }.getOrElse { emptyList() }
            if (tricks.isEmpty()) problems += "$n² has no trick"
            tricks.filterNot { endsAt(it, answer) }.forEach { problems += "$n²: '${it.name}' ends at '${it.steps.last()}'" }
        }
        assertTrue(problems.isEmpty(), "${problems.size} problems, first: ${problems.take(10)}")
    }
}
