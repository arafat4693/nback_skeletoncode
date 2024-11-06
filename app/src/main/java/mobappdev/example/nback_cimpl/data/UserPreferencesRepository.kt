package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import mobappdev.example.nback_cimpl.ui.viewmodels.UserSettings
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository (
    private val dataStore: DataStore<Preferences>
){
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val NUM_EVENTS = intPreferencesKey("num_events")
        val EVENT_INTERVAL = longPreferencesKey("event_interval")
        val N_BACK = intPreferencesKey("n_back")
        val GRID_SIZE = intPreferencesKey("grid_size")
        val NUM_LETTERS = intPreferencesKey("num_letters")
        const val TAG = "UserPreferencesRepo"
    }

    val highscore: Flow<Int> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[HIGHSCORE] ?: 0
        }

    val userSettings: Flow<UserSettings> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            UserSettings(
                numEvents = preferences[NUM_EVENTS] ?: 10,
                eventInterval = preferences[EVENT_INTERVAL] ?: 2000L,
                nBack = preferences[N_BACK] ?: 1,
                gridSize = preferences[GRID_SIZE] ?: 3,
                numLetters = preferences[NUM_LETTERS] ?: 26
            ).also { Log.d(TAG, "Loaded settings: $it") } // Log for debugging
        }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }

    suspend fun saveUserSettings(settings: UserSettings) {
        dataStore.edit { preferences ->
            preferences[NUM_EVENTS] = settings.numEvents
            preferences[EVENT_INTERVAL] = settings.eventInterval
            preferences[N_BACK] = settings.nBack
            preferences[GRID_SIZE] = settings.gridSize
            preferences[NUM_LETTERS] = settings.numLetters
            Log.d(TAG, "Settings saved: $settings") // Log for debugging
        }
    }
}