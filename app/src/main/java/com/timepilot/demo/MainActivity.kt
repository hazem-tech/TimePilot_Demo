package com.timepilot.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    // This i bad i should change it but for now i think that is fine
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            Databases::class.java,
            "time_pilot_events.db"
        ).build()
    }
    private val viewModel by viewModels<EventsViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventsViewModel(db.dao) as T
                }
            }
        }
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimePilotDemoTheme {
                Scaffold { padding ->
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()

                    val colorsOptions = listOf(
                        Pair("Main",
                            if (!isSystemInDarkTheme()) colorResource(com.google.android.material.R.color.material_dynamic_primary90)
                            else colorResource(com.google.android.material.R.color.material_dynamic_primary10)),
                        Pair("Red", if (!isSystemInDarkTheme()) Color(0xFFF6BFBF) else Color(0xFF301B21)),
                        Pair("Green", if (!isSystemInDarkTheme()) Color(0xFFD1EDD6) else Color(0xFF1D291A) ),
                        Pair("Blue", if (!isSystemInDarkTheme()) Color(0xFFCAD6F4) else Color(0xFF122038)),
                        Pair("Yellow", if (!isSystemInDarkTheme()) Color(0xFFFDD9AB) else Color(0xFF372C17)),
                        Pair("Pink", if (!isSystemInDarkTheme()) Color(0xFFF0BBDB) else Color(0xFF3A1F39)),
                        Pair("Purple", if (!isSystemInDarkTheme()) Color(0xFFD9C1F3) else Color(0xFF201731))
                    )

                    // todo show onboarding screen if the user never created an event like if it is empty, idk if the user saw it before the only way to now show it is to have at least one event in the past at any date

                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        enterTransition = { slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(300, easing = customEasing), initialOffset = {300}) + fadeIn(tween(200)) },
                        exitTransition = { slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(300, easing = customEasing), targetOffset = {-300}) + fadeOut(tween(200)) },
                        popEnterTransition = { slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(300, easing = customEasing), initialOffset = {-300}) + fadeIn(tween(200)) },
                        popExitTransition = { slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(300, easing = customEasing), targetOffset = {300}) + fadeOut(tween(200)) }
                    ) {
                        navigation(
                            startDestination = "settingsScreen",
                            route = "settings"
                        ) {
                            composable("settingsScreen") {
                                SettingsScreen(navController)
                            }
                            composable("strictModeScreen") {
                                StrictMode(navController)
                            }
                            composable("alwaysAllowedApps") {
                                Scaffold(
                                    topBar = {
                                        TopAppBar(
                                            title = {
                                                Text("Always allowed apps")
                                            },
                                            navigationIcon = {
                                                IconButton(onClick = { navController.popBackStack() }) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Localized description"
                                                    )
                                                }
                                            }
                                        )
                                    },
                                ) { innerPadding ->
                                    Box(Modifier.padding(innerPadding)) {
                                        AppsScreen(state, viewModel::onEvent)
                                    }
                                }
                            }
                        }

                        composable("home") {
                            EventsList(state, viewModel::onEvent, colors = colorsOptions, padding, navController)
                        }
                    }
                }
            }
        }
    }
}

val customEasing: Easing = Easing { fraction ->
    val p0 = 0f
    val p1 = 1f
    val p2 = 1f
    val p3 = 1f
    (1 - fraction).pow(500) * p0 + 3 * (1 - fraction).pow(2) * fraction * p1 + 3 * (1 - fraction) * fraction.pow(
        2
    ) * p2 + fraction.pow(3) * p3
}