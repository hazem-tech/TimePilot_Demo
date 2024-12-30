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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimePilotDemoTheme {
                Scaffold { padding ->
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
                        Pair("Main",
                            if (!isSystemInDarkTheme()) colorResource(com.google.android.material.R.color.material_dynamic_primary80)
                            else colorResource(com.google.android.material.R.color.material_dynamic_primary10)),
                        Pair("Red", if (!isSystemInDarkTheme()) Color(0xFFF5BABA) else Color(0xFF341314)),
                        Pair("Green", if (!isSystemInDarkTheme()) Color(0xFFBAF5CC) else Color(0xFF143413) ),
                        Pair("Blue", if (!isSystemInDarkTheme()) Color(0xFFB4CDFF) else Color(0xFF122038)),
                        Pair("Yellow", if (!isSystemInDarkTheme()) Color(0xFFFDD9AB) else Color(0xFF443902)),
                        Pair("Pink", if (!isSystemInDarkTheme()) Color(0xFFFFB1DB) else Color(0xFF341235)),
                        Pair("Purple", if (!isSystemInDarkTheme()) Color(0xFFDEB7FF) else Color(0xFF23103D))
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
                                // TODO()
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
                                        AppsScreen(state, viewModel::onEvent, allApps)
                                    }
                                }
                            }
                        }

                        composable("home") {
                            EventsList(state, viewModel::onEvent, allApps, colors = colorsOptions, padding, navController)
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