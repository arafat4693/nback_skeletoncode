package mobappdev.example.nback_cimpl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mobappdev.example.nback_cimpl.ui.screens.GameScreen
import mobappdev.example.nback_cimpl.ui.screens.HomeScreen
import mobappdev.example.nback_cimpl.ui.screens.SettingsScreen
import mobappdev.example.nback_cimpl.ui.theme.NBack_CImplTheme
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM

/**
 * This is the MainActivity of the application
 *
 * Your navigation between the two (or more) screens should be handled here
 * For this application you need at least a homescreen (a start is already made for you)
 * and a gamescreen (you will have to make yourself, but you can use the same viewmodel)
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NBack_CImplTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Instantiate the ViewModel
                    val gameViewModel: GameVM = viewModel(
                        factory = GameVM.Factory
                    )

                    // Setup Navigation Controller
                    val navController = rememberNavController()

                    // Define Navigation Graph
                    NavHost(
                        navController = navController,
                        startDestination = "home" // Start at the HomeScreen
                    ) {
                        // HomeScreen Route
                        composable("home") {
                            HomeScreen(
                                vm = gameViewModel,
                                onStartGame = { gameType ->
                                    gameViewModel.setGameType(gameType)
                                    gameViewModel.startGame()
                                    navController.navigate("game")
                                },
                                toSetting = {navController.navigate("setting")}
                            )
                        }

                        // GameScreen Route
                        composable("game") {
                            GameScreen(
                                vm = gameViewModel,
                                onGameEnd = {
                                    // Navigate back to HomeScreen on game end
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // SettinsScreen Route
                        composable("setting") {
                            SettingsScreen(
                                vm = gameViewModel,
                                onBack = {
                                    // Navigate back to HomeScreen on game end
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}