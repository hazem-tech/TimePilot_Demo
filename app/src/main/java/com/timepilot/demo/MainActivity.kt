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
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import kotlinx.coroutines.launch
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimePilotDemoTheme {
                val navController = rememberNavController()
                var allApps by remember { mutableStateOf(listOf<App>()) }
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    scope.launch {
                        allApps = getInstalledApps(this@MainActivity).sortedBy { it.name }
                        Log.d("AllAppsUpdating", allApps.toString())
                    }
                }

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
                            AppsScreen(allApps)
                        }
                    }

                    composable("home") {
                        EventsList(allApps, navController)
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