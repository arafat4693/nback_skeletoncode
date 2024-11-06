package mobappdev.example.nback_cimpl.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import mobappdev.example.nback_cimpl.ui.viewmodels.UserSettings

@Composable
fun SettingsScreen(vm: GameViewModel, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    val userSettings by vm.userSettings.collectAsState()
    var numEvents by remember { mutableStateOf(userSettings.numEvents) }
    var eventInterval by remember { mutableStateOf(userSettings.eventInterval) }
    var nBack by remember { mutableStateOf(userSettings.nBack) }
    var gridSize by remember { mutableStateOf(userSettings.gridSize) }
    var numLetters by remember { mutableStateOf(userSettings.numLetters) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)

        // Slider for Number of Events
        Text("Number of Events: $numEvents")
        Slider(
            value = numEvents.toFloat(),
            onValueChange = { numEvents = it.toInt() },
            valueRange = 5f..30f
        )

        // Slider for Event Interval
        Text("Time Between Events: ${eventInterval}ms")
        Slider(
            value = eventInterval.toFloat(),
            onValueChange = { eventInterval = it.toLong() },
            valueRange = 500f..5000f
        )

        // Slider for N-Back
        Text("N-Back Value: $nBack")
        Slider(
            value = nBack.toFloat(),
            onValueChange = { nBack = it.toInt() },
            valueRange = 1f..5f
        )

        // Slider for Grid Size
        Text("Grid Size: ${gridSize}x$gridSize")
        Slider(
            value = gridSize.toFloat(),
            onValueChange = { gridSize = it.toInt() },
            valueRange = 3f..6f
        )

        // Slider for Number of Letters
        Text("Number of Spoken Letters: $numLetters")
        Slider(
            value = numLetters.toFloat(),
            onValueChange = { numLetters = it.toInt() },
            valueRange = 5f..26f
        )


        // Save and Back Button
        Button(onClick = {
            // Save settings persistently
            vm.updateSettings(
                events = numEvents,
                interval = eventInterval,
                n = nBack,
                size = gridSize,
                letters = numLetters
            )
            Log.d("SettingsScreen", "Settings to save: events=$numEvents, interval=$eventInterval, n=$nBack, size=$gridSize, letters=$numLetters")
            onBack()
        }) {
            Text("Save")
        }
    }
}
