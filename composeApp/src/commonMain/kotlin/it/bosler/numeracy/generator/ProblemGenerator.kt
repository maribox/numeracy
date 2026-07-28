package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType
import kotlin.random.Random

interface ProblemGenerator {
    fun generate(): Problem
}

/**
 * The generator for a scenario. [rng] is the source of every question it makes; a seeded one
 * gives the same run of questions twice, which is what draws a screen the same way twice.
 */
fun generatorFor(
    scenarioType: ScenarioType,
    difficulty: Difficulty = Difficulty.NORMAL,
    rng: Random = Random.Default,
): ProblemGenerator = when (scenarioType) {
    ScenarioType.DARTS -> DartsGenerator(rng)
    ScenarioType.BLACKJACK -> BlackjackGenerator(rng)
    ScenarioType.POT_ODDS -> PokerGenerator(difficulty, rng)
    ScenarioType.DRAW_EQUITY -> DrawEquityGenerator(difficulty, rng)
    ScenarioType.MAKING_CHANGE -> MakingChangeGenerator(rng)
    ScenarioType.CURRENCY_EXCHANGE -> CurrencyExchangeGenerator(rng)
    ScenarioType.TIME_ZONES -> TimeZonesGenerator(rng)
    ScenarioType.LENGTH_CONVERSION -> LengthConversionGenerator(rng)
    ScenarioType.WEIGHT_CONVERSION -> WeightConversionGenerator(rng)
    ScenarioType.TEMPERATURE_CONVERSION -> TemperatureConversionGenerator(rng)
    ScenarioType.VOLUME_CONVERSION -> VolumeConversionGenerator(rng)
    ScenarioType.SPEED_CONVERSION -> SpeedConversionGenerator(rng)
    ScenarioType.DOOMSDAY -> DoomsdayGenerator(rng)
    ScenarioType.SQUARING -> SquaringGenerator(rng)
    ScenarioType.MULTIPLICATION -> MultiplicationGenerator(rng)
}
