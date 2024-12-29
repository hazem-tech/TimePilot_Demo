package com.timepilot.demo

import android.content.Context
import android.content.Context.LAUNCHER_APPS_SERVICE
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Process
import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import kotlinx.coroutines.launch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimePilotDemoTheme {
                val state by viewModel.state.collectAsState()
                val navController = rememberNavController()
                var allApps by remember { mutableStateOf(listOf<App>()) }
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    scope.launch {
                        allApps = getInstalledApps(this@MainActivity).sortedBy { it.name }
                        Log.d("AllAppsUpdating", allApps.toString())
                    }
                }

                val colorsOptions = listOf(
                    ColorOption(
                        name = "Main",
                        backgroundColor = if (!isSystemInDarkTheme()) colorResource(com.google.android.material.R.color.material_dynamic_secondary90)
                        else colorResource(com.google.android.material.R.color.material_dynamic_secondary10),
                        backgroundBarColor = if (!isSystemInDarkTheme()) colorResource(com.google.android.material.R.color.material_dynamic_primary80)
                        else colorResource(com.google.android.material.R.color.material_dynamic_primary10),
                        buttonColor = MaterialTheme.colorScheme.primary.copy(0.3f)
                    ),
                    ColorOption(
                        name = "Red",
                        backgroundColor = if (!isSystemInDarkTheme()) Color(0xFFFFD8D8) else Color(0xFF221818),
                        backgroundBarColor = if (!isSystemInDarkTheme()) Color(0xFFFFC3C3) else Color(0xFF2E1214),
                        buttonColor = if (!isSystemInDarkTheme()) Color(0xFFF4DFDF) else Color(0xFF3A212A)
                    ),
                    ColorOption(
                        name = "Green",
                        backgroundColor = if (!isSystemInDarkTheme()) Color(0xFFD3E8E9) else Color(0xFF000000),
                        backgroundBarColor = Color.Black, // Simplified for consistent dark mode
                        buttonColor = Color.Black
                    ),
                    ColorOption(
                        name = "Blue",
                        backgroundColor = if (!isSystemInDarkTheme()) Color(0xFFD8E3FF) else Color(0xFF15171D),
                        backgroundBarColor = if (!isSystemInDarkTheme()) Color(0xFFB4CDFF) else Color(0xFF121A2D),
                        buttonColor = if (!isSystemInDarkTheme()) Color(0xFFC7DCF5) else Color(0xFF20263A)
                    ),
                    ColorOption(
                        name = "Yellow",
                        backgroundColor = if (!isSystemInDarkTheme()) Color(0xFFEAE4D5) else Color(0xFF1F160E),
                        backgroundBarColor = if (!isSystemInDarkTheme()) Color(0xFFF0CFA4) else Color(0xFF2A2010),
                        buttonColor = if (!isSystemInDarkTheme()) Color(0xFFF6EEE0) else Color(0xFF3B321E)
                    ),
                    ColorOption(
                        name = "Pink",
                        backgroundColor = if (!isSystemInDarkTheme()) Color(0xFFFFD8ED) else Color.Black,
                        backgroundBarColor = Color.Black,
                        buttonColor = Color.Black
                    ),
                    ColorOption(
                        name = "Purple",
                        backgroundColor = if (!isSystemInDarkTheme()) Color(0xFFF1D8FF) else Color.Black,
                        backgroundBarColor = Color.Black,
                        buttonColor = Color.Black
                    )
                )

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
                            // TODO()
                        }
                        composable("alwaysAllowedApps") {
                            AppsScreen(state, viewModel::onEvent, allApps)
                        }
                    }

                    composable("home") {
                        EventsList(state, viewModel::onEvent, allApps, colors = colorsOptions, navController)
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

private fun getInstalledApps(context: Context) : List<App> {
    val launcherApps = context.getSystemService(LAUNCHER_APPS_SERVICE) as LauncherApps
    val userHandle = Process.myUserHandle()
    val activityList = launcherApps.getActivityList(null, userHandle)
    val allApps = mutableListOf<App>()

    for (activity in activityList) {
        val appName = activity.label.toString()
        val appIcon = activity.getIcon(0)
        val packageName = activity.applicationInfo.packageName
        // convert drawable to bitmap
        val bitmap = Bitmap.createBitmap(
            appIcon.intrinsicWidth,
            appIcon.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        appIcon.setBounds(0, 0, canvas.width, canvas.height)
        appIcon.draw(canvas)

        allApps.add(App(name = appName, packageName = packageName, icon = BitmapPainter(bitmap.asImageBitmap())))
    }
    allApps.sortBy { it.name }
    return allApps
    //val serviceIntent = Intent(this, UsageEventsService::class.java)
    //startForegroundService(serviceIntent)
}