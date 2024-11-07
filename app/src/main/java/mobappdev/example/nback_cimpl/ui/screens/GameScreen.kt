package mobappdev.example.nback_cimpl.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import java.util.Locale

// @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GameScreen(
    vm: GameViewModel,
    onGameEnd: () -> Unit // Callback to navigate back to HomeScreen
) {
    val gameState by vm.gameState.collectAsState()
    val userSettings by vm.userSettings.collectAsState()
    val score by vm.score.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State to manage the color of the "Check Match" button for feedback
    var audioButtonColor by remember { mutableStateOf(Color(0xFFCC8899)) }
    var visualButtonColor by remember { mutableStateOf(Color(0xFFCC8899)) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GameStatus(
                currentEventIndex = gameState.currentEventIndex + 1,
                correctVisualMatches = gameState.correctVisualMatches,
                incorrectVisualMatches = gameState.incorrectVisualMatches,
                correctAudioMatches = gameState.correctAudioMatches,
                incorrectAudioMatches = gameState.incorrectAudioMatches,
                gameType = gameState.gameType
            )

            Text(text = "Score: $score", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            // Visual stimuli display for 3x3 grid
            if (gameState.gameType == GameType.Visual || gameState.gameType == GameType.AudioVisual) {
                VisualStimuliGrid(eventValue = gameState.visualEventValue, gridSize = userSettings.gridSize)
            }

            // Audio playback for auditory stimuli
            if (gameState.gameType == GameType.Audio || gameState.gameType == GameType.AudioVisual) {
                AudioStimuliPlayer(eventValue = gameState.audioEventValue, eventIndex = gameState.currentEventIndex, numLetters = userSettings.numLetters)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gameState.gameType == GameType.Visual || gameState.gameType == GameType.AudioVisual) {
                    Button(
                        onClick = {
                            val isCorrect = vm.checkVisualMatch()
                            if (!isCorrect) {
                                visualButtonColor = Color.Red // Change color for incorrect match
                                scope.launch {
                                    // Reset color after a short delay
                                    kotlinx.coroutines.delay(500)
                                    visualButtonColor = Color(0xFFCC8899)
                                }
                            } },
                        modifier = Modifier.padding(3.dp),
                        colors = ButtonDefaults.buttonColors(visualButtonColor)
                    ) {
                        Text(text = "Check Visual Match")
                    }
                }

                if (gameState.gameType == GameType.Audio || gameState.gameType == GameType.AudioVisual) {
                    Button(
                        onClick = {
                            val isCorrect = vm.checkAudioMatch()
                            if (!isCorrect) {
                                audioButtonColor = Color.Red // Change color for incorrect match
                                scope.launch {
                                    // Reset color after a short delay
                                    kotlinx.coroutines.delay(500)
                                    audioButtonColor = Color(0xFFCC8899)
                                }
                            } },
                        modifier = Modifier.padding(3.dp),
                        colors = ButtonDefaults.buttonColors(audioButtonColor)
                    ) {
                        Text(text = "Check Audio Match")
                    }
                }
            }

            Button(
                onClick = {vm.startGame()},
                modifier = Modifier.padding(3.dp)
            ) {
                Text("Start New Round")
            }

            Button(
                onClick = onGameEnd,
                modifier = Modifier.padding(3.dp)
            ) {
                Text(text = "End Game")
            }
        }
    }
}


@Composable
fun VisualStimuliGrid(eventValue: Int, gridSize: Int) {
    val totalItems = gridSize * gridSize

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier.size(400.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(totalItems) { index ->
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = if (index+1 == eventValue) Color.Yellow else Color(0xFFB0C4DE),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}



@Composable
fun AudioStimuliPlayer(
    eventValue: Int,
    eventIndex: Int,
    numLetters: Int,
    context: Context = LocalContext.current // Get the current context
) {
    // Initialize TextToSpeech
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    val initialized = remember { mutableStateOf(false) }

    // Create the TTS instance once when this composable enters the composition
    DisposableEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                initialized.value = true
            }
        }

        // Clean up the TTS instance when this composable leaves the composition
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    // Generate the letter to be spoken based on eventValue
    val letter = 'A' + (eventValue % numLetters)
    Text("Letter: $letter")

    // Speak the letter each time eventValue changes, if TTS is initialized
    LaunchedEffect(eventValue+eventIndex, initialized.value) {
        if (initialized.value) {
            textToSpeech?.speak(
                letter.toString(),
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }
    }
}


@Composable
fun GameStatus(currentEventIndex: Int, correctVisualMatches: Int, incorrectVisualMatches: Int, correctAudioMatches: Int, incorrectAudioMatches: Int, gameType: GameType) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Current Event Number: $currentEventIndex")
        if (gameType == GameType.Visual || gameType == GameType.AudioVisual) {
            Text("Correct Visual Matches: $correctVisualMatches")
            Text("Incorrect Visual Matches: $incorrectVisualMatches")
        }
        if (gameType == GameType.Audio || gameType == GameType.AudioVisual) {
            Text("Correct Audio Matches: $correctAudioMatches")
            Text("Incorrect Audio Matches: $incorrectAudioMatches")
        }
    }
}