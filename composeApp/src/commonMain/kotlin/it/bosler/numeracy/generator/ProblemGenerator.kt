package it.bosler.numeracy.generator

import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType

interface ProblemGenerator {
    fun generate(): Problem
}

fun generatorFor(scenarioType: ScenarioType): ProblemGenerator = when (scenarioType) {
    ScenarioType.DARTS -> DartsGenerator()
    ScenarioType.BLACKJACK -> BlackjackGenerator()
    ScenarioType.POT_ODDS -> PokerGenerator()
    ScenarioType.OUTS_COUNTING -> OutsCountingGenerator()
    ScenarioType.EQUITY -> EquityGenerator()
    ScenarioType.IMPLIED_ODDS -> ImpliedOddsGenerator()
    ScenarioType.MAKING_CHANGE -> MakingChangeGenerator()
    ScenarioType.CURRENCY_EXCHANGE -> CurrencyExchangeGenerator()
    ScenarioType.TIME_ZONES -> TimeZonesGenerator()
    ScenarioType.LENGTH_CONVERSION -> LengthConversionGenerator()
    ScenarioType.WEIGHT_CONVERSION -> WeightConversionGenerator()
    ScenarioType.TEMPERATURE_CONVERSION -> TemperatureConversionGenerator()
    ScenarioType.VOLUME_CONVERSION -> VolumeConversionGenerator()
    ScenarioType.SPEED_CONVERSION -> SpeedConversionGenerator()
    ScenarioType.DOOMSDAY -> DoomsdayGenerator()
}
