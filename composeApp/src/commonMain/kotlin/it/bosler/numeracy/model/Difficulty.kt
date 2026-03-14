package it.bosler.numeracy.model

/**
 * # Difficulty Modes -Design Concept
 *
 * Every difficulty mode MUST produce an immediately visible, structural change in the
 * question display. No text hints during gameplay -only visual/game elements that
 * appear or disappear when switching modes. The (i) info sheet is the only place for
 * text-based explanations.
 *
 * ## Per-Scenario Difficulty Behavior
 *
 * ### DARTS (HARD, NORMAL, PRACTICE, LEARNING)
 * - **HARD**: Score odometer hidden after first correct answer (shows "???"). Reappears
 *   on mistakes. User must remember the running score. Pure mental math.
 * - **NORMAL**: Score visible. Throw shown as base number (e.g., "20" for Triple 20).
 *   After answering, the number morphs into the product (60) and flies up into the score
 *   as visual feedback.
 * - **PRACTICE**: Score visible. Product shown immediately on entrance (Triple 20 shows
 *   "60" right away). User sees what they're subtracting before they type.
 * - **LEARNING**: Score visible. Product shown immediately. Additionally shows a visual
 *   column subtraction breakdown: ones digit, tens digit, borrow indicators.
 *
 * ### BLACKJACK (NORMAL, PRACTICE)
 * - **NORMAL**: Cards shown in fan layout. User calculates the total from scratch.
 * - **PRACTICE**: Cards shown with helper badges -face card total shown (e.g., "Face: 20"),
 *   number card total shown (e.g., "Numbers: 13"), ace count shown. User just combines
 *   the groups and decides ace value.
 *
 * ### POKER (NORMAL, PRACTICE)
 * - **NORMAL**: Pot and call amount shown. User calculates pot odds percentage.
 * - **PRACTICE**: Total pot (pot + call) shown as extra badge. Simplified fraction shown
 *   (e.g., "1/4"). User just converts the fraction to a percentage.
 *
 * ### MAKING CHANGE (NORMAL, PRACTICE)
 * - **NORMAL**: Bill total and payment shown. User calculates full change amount.
 * - **PRACTICE**: The cents part of the change is shown on the receipt (e.g., "Cents: 65¢").
 *   User only needs to figure out the euro part and combine.
 *
 * ### CURRENCY EXCHANGE (NORMAL, PRACTICE)
 * - **NORMAL**: EUR amount + exchange rate shown. User multiplies.
 * - **PRACTICE**: Rate broken down visually. For rate 1.08: shows "×1 = 50" (the whole
 *   part pre-calculated). For rate <1: shows "subtract X%". User handles the remaining
 *   fractional part.
 *
 * ### TIME ZONES (NORMAL, PRACTICE)
 * - **NORMAL**: Source city + time, target city. User figures out the offset and converts.
 * - **PRACTICE**: A badge between the cities shows the time offset (e.g., "+3h" or "−5h 30m").
 *   User just adds/subtracts the shown offset.
 *
 * ### DOOMSDAY (NORMAL, PRACTICE, LEARNING)
 * - **NORMAL**: Calendar page showing date. User runs the full Doomsday algorithm.
 * - **PRACTICE**: Two helper badges shown: century anchor (e.g., "2000s → Tuesday") and
 *   year's doomsday (e.g., "2024 → Thursday"). User does month anchor + day counting.
 * - **LEARNING**: All of the above plus the month anchor date and its weekday are shown
 *   (e.g., "April 4th = Thursday"). User just counts days from the anchor.
 *
 * ## Implementation Rule
 * The question display composable for each scenario receives the current [Difficulty] and
 * conditionally renders helper elements. Switching difficulty mid-question must instantly
 * show/hide these elements with no delay.
 */
enum class Difficulty(val label: String) {
    HARD("Hard"),
    NORMAL("Normal"),
    PRACTICE("Practice"),
    LEARNING("Learning"),
}
