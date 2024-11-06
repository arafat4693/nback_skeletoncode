package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val userSettings: StateFlow<UserSettings>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>

    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkMatch() : Boolean
    fun updateSettings(events: Int, interval: Long, n: Int, size: Int, letters: Int)
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _userSettings = MutableStateFlow(UserSettings(10, 2000L, 1, 3, 26))
    override val userSettings: StateFlow<UserSettings>
        get() = _userSettings.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // nBack is currently hardcoded
    /*override var nBack: Int = 1
        private set*/

    private var job: Job? = null  // coroutine job for the game event

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    init {
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
                Log.d("GameVM", "highscore in VM: $it") // Log for debugging
            }
        }

        viewModelScope.launch {
            userPreferencesRepository.userSettings.collect { settings ->
                _userSettings.value = settings
                Log.d("GameVM", "Settings updated in VM: $settings") // Log for debugging
            }
        }
    }

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop

        val nBack = _userSettings.value.nBack
        val nrOfEvents = _userSettings.value.numEvents

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(nrOfEvents, 9, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(events)
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
            // Todo: update the highscore
            updateHighscore()
        }
    }

    private fun updateHighscore() {
        if (_score.value > _highscore.value) {
            _highscore.value = _score.value
            viewModelScope.launch {
                userPreferencesRepository.saveHighScore(_highscore.value)
            }
        }
    }

    override fun updateSettings(events: Int, interval: Long, n: Int, size: Int, letters: Int) {
        val newSettings = UserSettings(events, interval, n, size, letters)
        _userSettings.value = newSettings

        viewModelScope.launch {
            userPreferencesRepository.saveUserSettings(newSettings)
        }
    }

    override fun checkMatch() : Boolean {
        // Ensure only one match can be registered per event
        if (_gameState.value.isMatchChecked) return false

        val nBack = _userSettings.value.nBack

        // Check if the current event matches the event `n` steps back
        val currentIndex = _gameState.value.currentEventIndex
        val isCorrectMatch = currentIndex >= nBack && events[currentIndex] == events[currentIndex - nBack]

        if (isCorrectMatch) {
            _score.value += 1  // Correct match
            _gameState.value = _gameState.value.copy(
                correctMatches = _gameState.value.correctMatches + 1,
                isMatchChecked = true
            )
        } else {
            _score.value -= 1  // Incorrect match
            _gameState.value = _gameState.value.copy(
                incorrectMatches = _gameState.value.incorrectMatches + 1,
                isMatchChecked = true
            )
        }

        return isCorrectMatch
    }
    private suspend fun runAudioGame(events: Array<Int>) {
        // Reset the state before starting
        _score.value = 0
        _gameState.value = _gameState.value.copy(isMatchChecked = false, correctMatches = 0, incorrectMatches = 0)
        setGameType(GameType.Audio)

        for ((index, value) in events.withIndex()) {
            // Update the game state with the current event details
            _gameState.value = _gameState.value.copy(eventValue = value, currentEventIndex = index, isMatchChecked = false)

            // Display the visual stimulus (e.g., by updating the UI with `eventValue`)
            delay(_userSettings.value.eventInterval)  // Delay between events
        }
    }

    private suspend fun runVisualGame(events: Array<Int>){
        // Reset the state before starting
        _score.value = 0
        _gameState.value = _gameState.value.copy(isMatchChecked = false, correctMatches = 0, incorrectMatches = 0)
        setGameType(GameType.Visual)

        for ((index, value) in events.withIndex()) {
            // Update the game state with the current event details
            _gameState.value = _gameState.value.copy(eventValue = value, currentEventIndex = index, isMatchChecked = false)

            // Display the visual stimulus (e.g., by updating the UI with `eventValue`)
            delay(_userSettings.value.eventInterval)  // Delay between events
        }
    }

    private fun runAudioVisualGame(){

    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game (Visual, Audio, or AudioVisual)
    val eventValue: Int = -1,  // Current value of the event in the sequence
    val currentEventIndex: Int = 0,  // Current index in the sequence of events
    val isMatchChecked: Boolean = false,  // Prevents multiple matches for the same event
    val correctMatches: Int = 0,  // Number of correct matches
    val incorrectMatches: Int = 0  // Number of incorrect matches
)

data class UserSettings(
    val numEvents: Int,
    val eventInterval: Long,
    val nBack: Int,
    val gridSize: Int,
    val numLetters: Int
)

/*class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkMatch() {
    }
}*/