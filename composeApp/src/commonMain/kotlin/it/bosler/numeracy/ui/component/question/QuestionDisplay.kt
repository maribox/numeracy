package it.bosler.numeracy.ui.component.question

import androidx.compose.runtime.Composable
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.Problem
import it.bosler.numeracy.model.ScenarioType

@Composable
fun QuestionDisplay(problem: Problem, difficulty: Difficulty = Difficulty.NORMAL, hideScore: Boolean = false, answered: Boolean = false) {
    when (problem.scenarioType) {
        ScenarioType.DARTS -> DartsQuestionDisplay(problem, difficulty, hideScore, answered)
        ScenarioType.BLACKJACK -> BlackjackQuestionDisplay(problem, difficulty)
        ScenarioType.POT_ODDS -> PokerQuestionDisplay(problem, difficulty)
        ScenarioType.OUTS_COUNTING -> OutsCountingQuestionDisplay(problem, difficulty)
        ScenarioType.EQUITY -> EquityQuestionDisplay(problem, difficulty)
        ScenarioType.IMPLIED_ODDS -> ImpliedOddsQuestionDisplay(problem, difficulty)
        ScenarioType.MAKING_CHANGE -> MakingChangeQuestionDisplay(problem, difficulty)
        ScenarioType.CURRENCY_EXCHANGE -> CurrencyExchangeQuestionDisplay(problem, difficulty)
        ScenarioType.TIME_ZONES -> TimeZonesQuestionDisplay(problem, difficulty)
        ScenarioType.LENGTH_CONVERSION -> LengthConversionQuestionDisplay(problem, difficulty)
        ScenarioType.WEIGHT_CONVERSION -> WeightConversionQuestionDisplay(problem, difficulty)
        ScenarioType.TEMPERATURE_CONVERSION -> TemperatureConversionQuestionDisplay(problem, difficulty)
        ScenarioType.VOLUME_CONVERSION -> VolumeConversionQuestionDisplay(problem, difficulty)
        ScenarioType.SPEED_CONVERSION -> SpeedConversionQuestionDisplay(problem, difficulty)
        ScenarioType.DOOMSDAY -> DoomsdayQuestionDisplay(problem, difficulty)
    }
}
