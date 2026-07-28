package it.bosler.numeracy.gallery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import it.bosler.numeracy.model.AnswerRecord
import it.bosler.numeracy.model.AppData
import it.bosler.numeracy.model.Category
import it.bosler.numeracy.model.Difficulty
import it.bosler.numeracy.model.InputType
import it.bosler.numeracy.model.RunRecord
import it.bosler.numeracy.model.ScenarioType
import it.bosler.numeracy.persistence.AppContext
import it.bosler.numeracy.persistence.FileStorage
import it.bosler.numeracy.ui.screen.HomeScreen
import it.bosler.numeracy.ui.screen.PracticeScreen
import it.bosler.numeracy.ui.screen.ScenariosScreen
import it.bosler.numeracy.ui.screen.SettingsScreen
import it.bosler.numeracy.ui.screen.StatisticsScreen
import it.bosler.numeracy.ui.screen.SubcategoryScenariosScreen
import it.bosler.numeracy.viewmodel.PracticeViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random
import kotlin.system.exitProcess

/**
 * Every screen of the app, in every state it can be in, drawn off-screen with practice history that
 * is made here rather than played. The questions come from the real generators under a fixed seed,
 * so the same picture is drawn twice and a screen that changed is the only thing a redraw shows.
 */

private const val PHONE_W = 390
// A phone screen, or a taller one when checking whether a screen fits or merely scrolls.
private val PHONE_H = (System.getProperty("gallery.phoneHeight") ?: "844").toInt()
// Wide is read beside the upright picture, at half a window, so what matters is how few logical
// pixels it has rather than how many: at 1440 the text lands at half size and cannot be read.
private const val WIDE_W = 1180
private const val WIDE_H = 760

/** One picture: what it is called, how big, and how many pixels per dp. */
private data class Render(
    val suffix: String,
    val width: Int,
    val height: Int,
    val dark: Boolean,
    val scale: Float,
)

/**
 * A screen in one of the states it can be in. [state] becomes part of the file name, so
 * statistics-empty-phone-light.png says what it is without a table to look it up in. [history] is
 * whether the device it is drawn on has been practiced on, since half these screens say something
 * different on a fresh install.
 */
private data class Scene(
    val view: String,
    val state: String,
    val history: Boolean = true,
    val gameMode: Boolean = true,
    val content: @Composable () -> Unit,
)

private fun scene(
    view: String,
    state: String,
    history: Boolean = true,
    gameMode: Boolean = true,
    content: @Composable () -> Unit,
) = Scene(view, state, history, gameMode, content)

private val SCENES: List<Scene> = buildList {
    // ── Home ──────────────────────────────────────────────────────────────────
    // Home has one state: the categories are compiled into the app, so a fresh install shows
    // exactly this.
    add(scene("home", "as-it-is") { Home() })

    // ── Category ──────────────────────────────────────────────────────────────
    add(scene("category", "as-it-is") { Scenarios(Category.GAMES) })
    add(scene("category", "empty", history = false) { Scenarios(Category.GAMES) })
    add(scene("category", "conversions") { Scenarios(Category.CONVERSIONS) })

    // ── Subcategory ───────────────────────────────────────────────────────────
    add(scene("subcategory", "as-it-is") { Subcategory(Category.GAMES, "Poker") })
    add(scene("subcategory", "empty", history = false) { Subcategory(Category.GAMES, "Poker") })

    // ── Statistics ────────────────────────────────────────────────────────────
    add(scene("statistics", "as-it-is") { Statistics(ScenarioType.DARTS) })
    add(scene("statistics", "empty", history = false) { Statistics(ScenarioType.DARTS) })
    add(scene("statistics", "one-run") { Statistics(ScenarioType.SQUARING) })

    // ── Settings ──────────────────────────────────────────────────────────────
    add(scene("settings", "as-it-is") { Settings() })
    add(scene("settings", "empty", history = false, gameMode = false) { Settings() })

    // ── Practice, the screen the app is for ───────────────────────────────────
    add(scene("practice", "as-it-is") { Practice(ScenarioType.DARTS, correct = 4) })
    add(scene("practice", "empty") { Practice(ScenarioType.DARTS) })
    add(scene("practice", "wrong-answer") { Practice(ScenarioType.DARTS, correct = 4, wrong = 1) })
    // Five right in a row is answered with confetti over the whole screen, which is the one thing
    // the app does that a still picture of any other state does not admit to.
    add(scene("practice", "streak-reward") { Practice(ScenarioType.DARTS, correct = 5) })
    add(scene("practice", "info-sheet") { Practice(ScenarioType.DARTS, info = true) })
    add(scene("practice", "game-mode-off", gameMode = false) { Practice(ScenarioType.DARTS, correct = 5) })
    add(scene("practice", "hard") { Practice(ScenarioType.DARTS, Difficulty.HARD, correct = 3) })
    add(scene("practice", "practice-helpers") { Practice(ScenarioType.DARTS, Difficulty.PRACTICE, correct = 3) })
    add(scene("practice", "learning") { Practice(ScenarioType.DARTS, Difficulty.LEARNING, correct = 3) })
    add(scene("practice", "half-typed") { Practice(ScenarioType.DARTS, correct = 3, typed = "4") })
    // A conversion answered near enough: the scenarios with a tolerance accept it and show what the
    // exact answer was, which is the one moment a right answer does not move straight on.
    add(scene("practice", "close-answer") { Practice(ScenarioType.LENGTH_CONVERSION, correct = 2, close = true) })

    // One per scenario: each question is drawn by its own composable, and a book that shows one of
    // them says nothing about the other fourteen.
    add(scene("practice", "blackjack") { Practice(ScenarioType.BLACKJACK, correct = 2) })
    add(scene("practice", "draw-equity") { Practice(ScenarioType.DRAW_EQUITY, correct = 2) })
    add(scene("practice", "pot-odds") { Practice(ScenarioType.POT_ODDS, correct = 2) })
    add(scene("practice", "making-change") { Practice(ScenarioType.MAKING_CHANGE, correct = 2) })
    add(scene("practice", "currency-exchange") { Practice(ScenarioType.CURRENCY_EXCHANGE, correct = 2) })
    add(scene("practice", "time-zones") { Practice(ScenarioType.TIME_ZONES, correct = 2) })
    add(scene("practice", "doomsday") { Practice(ScenarioType.DOOMSDAY, correct = 2) })
    add(scene("practice", "length") { Practice(ScenarioType.LENGTH_CONVERSION, correct = 2) })
    add(scene("practice", "weight") { Practice(ScenarioType.WEIGHT_CONVERSION, correct = 2) })
    add(scene("practice", "temperature") { Practice(ScenarioType.TEMPERATURE_CONVERSION, correct = 2) })
    add(scene("practice", "volume") { Practice(ScenarioType.VOLUME_CONVERSION, correct = 2) })
    add(scene("practice", "speed") { Practice(ScenarioType.SPEED_CONVERSION, correct = 2) })
    add(scene("practice", "squaring") { Practice(ScenarioType.SQUARING, correct = 2) })
    add(scene("practice", "multiplication") { Practice(ScenarioType.MULTIPLICATION, correct = 2) })
}

// ── The screens, each taking the state it is being drawn in ──────────────────

@Composable
private fun Home() = HomeScreen(onCategorySelected = {}, onSettingsSelected = {})

@Composable
private fun Scenarios(category: Category) = ScenariosScreen(
    category = category,
    onScenarioSelected = {},
    onStatsSelected = {},
    onSubcategorySelected = {},
    onBack = {},
)

@Composable
private fun Subcategory(category: Category, subcategory: String) = SubcategoryScenariosScreen(
    category = category,
    subcategory = subcategory,
    onScenarioSelected = {},
    onStatsSelected = {},
    onBack = {},
)

@Composable
private fun Statistics(scenarioType: ScenarioType) =
    StatisticsScreen(scenarioType = scenarioType, onPlay = {}, onBack = {})

@Composable
private fun Settings() = SettingsScreen(onBack = {})

/**
 * The practice screen after [correct] questions answered right and [wrong] wrong, with [typed] left
 * standing in the answer. The run is played through the view model rather than assembled, so the
 * state drawn is one the app can actually reach.
 */
@Composable
private fun Practice(
    scenarioType: ScenarioType,
    difficulty: Difficulty = Difficulty.NORMAL,
    correct: Int = 0,
    wrong: Int = 0,
    typed: String = "",
    info: Boolean = false,
    close: Boolean = false,
) {
    val viewModel = remember(scenarioType, difficulty) {
        PracticeViewModel(scenarioType, difficulty, Random(SEED)).also { model ->
            repeat(correct) { answerRight(model) }
            repeat(wrong) { answerWrong(model) }
            if (close) answerClose(model)
            if (typed.isNotEmpty()) model.onAnswerChanged(typed)
            if (info) model.toggleInfo()
            // In game mode the fire bar burns down on a timer of its own. Drawing takes seconds of
            // real time, so left running it would put a different flame in the picture on every
            // machine; cleared, the run stops where the answers left it.
            ViewModelStore().apply { put("gallery", model) }.clear()
        }
    }
    PracticeScreen(scenarioType = scenarioType, onBack = {}, viewModel = viewModel)
}

private const val SEED = 20260728

private fun answerRight(model: PracticeViewModel) {
    val problem = model.state.value.currentProblem
    model.onAnswerChanged(problem.correctAnswer)
    if (problem.inputType == InputType.TIME) model.onSubmit()
}

/**
 * An answer one out: inside the tolerance of a scenario that has one, and the same length as the
 * right answer, which is what submits it.
 */
private fun answerClose(model: PracticeViewModel) {
    val right = model.state.value.currentProblem.correctAnswer
    val number = right.toIntOrNull() ?: return
    val near = (number + 1).takeIf { it.toString().length == right.length } ?: (number - 1)
    model.onAnswerChanged(near.toString())
}

/** An answer far enough from the right one that a scenario's tolerance cannot accept it. */
private fun answerWrong(model: PracticeViewModel) {
    val problem = model.state.value.currentProblem
    val right = problem.correctAnswer
    val wrong = when (problem.inputType) {
        InputType.WEEKDAY -> if (right == "Monday") "Thursday" else "Monday"
        else -> right.map { if (it.isDigit()) '0' + ((it - '0') + 5) % 10 else it }.joinToString("")
    }
    model.onAnswerChanged(wrong)
    if (problem.inputType == InputType.TIME) model.onSubmit()
}

// ── The device the screens are drawn on ──────────────────────────────────────

/**
 * A practice history to draw the screens that show one. Runs are written rather than played: what
 * they have to be is a fixed shape of accuracy, streak and speed, and playing them would tie every
 * statistics picture to whatever the generators happened to ask.
 */
private fun practiceHistory(): AppData {
    val runs = buildList {
        // Darts is the worn-in scenario: enough runs for every badge the screen can award.
        repeat(12) { index ->
            add(
                run(
                    id = "darts-$index",
                    scenario = ScenarioType.DARTS,
                    answers = 10,
                    wrongAt = if (index % 4 == 0) emptySet() else setOf(2, 7),
                    millis = 2400L + index * 60,
                )
            )
        }
        repeat(4) { index ->
            add(run("blackjack-$index", ScenarioType.BLACKJACK, 8, setOf(3), 2100L + index * 90))
        }
        repeat(3) { index ->
            add(run("draw-equity-$index", ScenarioType.DRAW_EQUITY, 6, setOf(1, 4), 5200L + index * 200))
        }
        repeat(2) { index ->
            add(run("pot-odds-$index", ScenarioType.POT_ODDS, 6, setOf(2), 6100L + index * 150))
        }
        add(run("squaring-0", ScenarioType.SQUARING, 5, setOf(3), 7300L))
        add(run("time-zones-0", ScenarioType.TIME_ZONES, 7, setOf(1, 5), 4400L))
        add(run("making-change-0", ScenarioType.MAKING_CHANGE, 9, setOf(6), 3100L))
    }
    return AppData(runs = runs, gameModeEnabled = true)
}

private fun run(
    id: String,
    scenario: ScenarioType,
    answers: Int,
    wrongAt: Set<Int>,
    millis: Long,
): RunRecord = RunRecord(
    id = id,
    scenarioType = scenario.name,
    startedAt = FIRST_RUN_AT,
    endedAt = FIRST_RUN_AT + answers * millis,
    answers = (0 until answers).map { index ->
        AnswerRecord(
            questionText = "${scenario.displayName} question ${index + 1}",
            correctAnswer = "42",
            userAnswer = if (index in wrongAt) "24" else "42",
            isCorrect = index !in wrongAt,
            timeMillis = millis + index * 120,
        )
    },
)

private const val FIRST_RUN_AT = 1_760_000_000_000L

/**
 * Points the app's storage at a throwaway home and puts the given history in it. The app keeps its
 * runs under the user's home, and a gallery that drew from there would draw whoever ran it.
 */
private fun useDevice(root: File, data: AppData) {
    root.mkdirs()
    System.setProperty("user.home", root.absolutePath)
    File(root, ".numeracy").mkdirs()
    File(root, ".numeracy/numeracy_data.json").writeText(Json.encodeToString(data))
    AppContext.initialize(FileStorage())
}

fun main() {
    val outDir = File(System.getProperty("gallery.out") ?: "build/gallery")
    val homes = File(System.getProperty("gallery.homes") ?: "build/gallery-home")
    // A change to one screen does not need the other thirty drawn again.
    val only = System.getProperty("gallery.only")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    val shapes = (System.getProperty("gallery.shapes") ?: "phone,wide").split(",").map { it.trim() }.toSet()
    val themes = (System.getProperty("gallery.themes") ?: "light,dark").split(",").map { it.trim() }.toSet()
    val started = System.currentTimeMillis()

    // A screen takes a few seconds of arithmetic to draw and Compose will not draw two at once in
    // one process, so the work is split into shards and a shard is one process. What each shard
    // takes is fixed by position, so two runs of the same shard draw the same screens.
    val shards = (System.getProperty("gallery.shards") ?: "1").toInt()
    val shard = (System.getProperty("gallery.shard") ?: "0").toInt()

    val wanted = SCENES
        .filter { only == null || it.view in only || "${it.view}-${it.state}" in only }
        .filterIndexed { index, _ -> index % shards == shard }
    if (wanted.isEmpty()) {
        if (only != null && shards == 1) {
            println("no scene matches ${only.joinToString(",")}; views: ${SCENES.map { it.view }.distinct().joinToString(",")}")
        }
        return
    }

    var drawn = 0
    // Which device a screen is drawn on is a global the app reads through, so scenes that want the
    // same one are drawn together and the groups run one after another.
    for ((device, group) in wanted.groupBy { it.history to it.gameMode }) {
        val (history, gameMode) = device
        val data = if (history) practiceHistory() else AppData()
        val home = "${if (history) "played" else "fresh"}-${if (gameMode) "game" else "plain"}-$shard"
        useDevice(File(homes, home), data.copy(gameModeEnabled = gameMode))

        for (scene in group) {
            // The screen as it is keeps the plain name; a state carries its own.
            val stem = if (scene.state == "as-it-is") scene.view else "${scene.view}-${scene.state}"
            val jobs = buildList {
                for (theme in listOf("light", "dark")) {
                    if (theme !in themes) continue
                    val dark = theme == "dark"
                    // Both shapes in both themes, with the theme in the name: a book read in the
                    // dark that falls back to whichever render does not say "light" ends up showing
                    // some screens light and some dark.
                    if ("phone" in shapes) add(Render("phone-$theme", PHONE_W, PHONE_H, dark, 2f))
                    if ("wide" in shapes) add(Render("wide-$theme", WIDE_W, WIDE_H, dark, 1.5f))
                }
            }
            for (job in jobs) {
                runCatching {
                    renderToPng(
                        "$stem-${job.suffix}", job.width, job.height,
                        dark = job.dark, outDir = outDir, scale = job.scale, content = scene.content,
                    )
                    drawn++
                }.onFailure { println("FAILED $stem-${job.suffix}: $it") }
            }
        }
    }
    println("$drawn renders of ${wanted.size} scenes in ${(System.currentTimeMillis() - started) / 1000.0}s")
    println("gallery written to ${outDir.absolutePath}")
    // A practice screen keeps a coroutine burning its fire bar down for as long as it exists, and
    // nothing here ever leaves that screen, so the process is ended rather than waited on.
    exitProcess(0)
}
