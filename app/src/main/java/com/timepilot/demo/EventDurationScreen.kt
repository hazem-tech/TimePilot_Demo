package com.timepilot.demo

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDuration(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
    navController: NavController
) {
    val minTimes by remember { mutableStateOf(
        listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 90, 120,
            150, 180, 210, 240, 300, 360, 420, 480, 540, 600, 660, 720))
    }
    var maxTimes by remember { mutableStateOf(minTimes.drop(1)) } // max can't be zero
    val minPagerState = rememberPagerState(pageCount = { minTimes.size }, initialPage = minTimes.indexOf(state.minTime))
    val maxPagerState = rememberPagerState(pageCount = { maxTimes.size }, initialPage = maxTimes.indexOf(state.maxTime))
    var maxCurrentValue: Int

    Column(Modifier.fillMaxSize()) {
        LargeTopAppBar(
            title = { Text("${state.eventName} duration") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            },
            windowInsets = WindowInsets(0.dp),
            modifier = Modifier.padding(bottom = 22.dp)
        )

        for (i in 0..1) {
            Text(
                text = if (i == 0) "Minimum" else "Maximum",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )

            Box(Modifier.padding(bottom = 24.dp)) {
                NumPicker(
                    state = if (i == 0) minPagerState else maxPagerState,
                    text = { index ->
                        val num = if (i == 0) minTimes[index] else maxTimes[index]
                        durationFormatting(num)
                    },
                    horizontal = true,
                    padding = PaddingValues(horizontal = 150.dp),
                    textHeight = 50.dp
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(height = 45.dp, width = 100.dp)
                        .border(
                            2.dp, Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                )
                            ),
                            CircleShape
                        )
                )
            }

            LaunchedEffect(minPagerState.settledPage) {
                onEvent(EventActions.SetMinTime(minTimes[minPagerState.settledPage]))

                // affect max list
                maxCurrentValue = maxTimes[maxPagerState.currentPage]
                maxTimes = if (minPagerState.settledPage > 0) minTimes.drop(minPagerState.settledPage) else minTimes.drop(1) // max can't be zero
                maxPagerState.scrollToPage(if (maxTimes[0] <= maxCurrentValue) maxTimes.indexOf(maxCurrentValue) else 0)
            }

            LaunchedEffect(maxPagerState.settledPage) {
                onEvent(EventActions.SetMaxTime(maxTimes[maxPagerState.settledPage]))
            }
        }
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("Tracking method") },
            leadingContent = {
                Icon(
                    Icons.Outlined.AvTimer,
                    contentDescription = null,
                )
            },
            trailingContent = { Text(state.trackingMode) },
            modifier = Modifier.clickable(onClick = {
                navController.navigate("eventTrackingScreen") {
                    launchSingleTop = true
                }
            })
        )

        Spacer(modifier = Modifier.weight(1f))
        ListItem(
            headlineContent = {
                Column {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        Modifier.padding(bottom = 15.dp)
                    )
                    Text(
                        "${state.eventName} can only be marked complete after minimum duration. And it will automatically complete after maximum duration",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 50.dp)
                    )
                }
            },
            colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.onSurfaceVariant),
        )
    }
}

fun durationFormatting(num: Int, simple: Boolean = false): String {
    val min = num % 60
    val hrs = num / 60
    return when {
        hrs == 0 -> "$min" + if (!simple) " min" else "m"
        min == 0 -> if (!simple) {
            if (hrs == 1) "1 hr" else "$hrs hrs"
        } else {
            "${hrs}h"
        }
        else -> "${hrs}h ${min}m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTracking(state: EventsStates, onEvent: (EventActions) -> Unit, navController: NavController) {
    val options = listOf(
        Triple("App Usage", "Ensures you spend the specified duration actively using the selected apps", Icons.Outlined.Leaderboard),
        Triple("Countdown", "A timer runs when you start the event for the specified duration", Icons.Outlined.Timer)
    )

    Column {
        LargeTopAppBar(
            title = { Text("How duration is tracked") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Button"
                    )
                }
            },
            windowInsets = WindowInsets(0.dp)
        )

        options.forEach { (name, description, icon) ->
            ListItem(
                headlineContent = { Text(name) },
                leadingContent = { Icon(icon, contentDescription = null) },
                supportingContent = { Text(description) },
                trailingContent = { RadioButton(selected = state.trackingMode == name, onClick = { onEvent(EventActions.SetTrackingMode(name)) })},
                modifier = Modifier.clickable(onClick = { onEvent(EventActions.SetTrackingMode(name)) })
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DurationPreview() {
    TimePilotDemoTheme {
        EventDuration(EventsStates(), {}, rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun TrackingPreview() {
    TimePilotDemoTheme {
        EventTracking(EventsStates(), {}, rememberNavController())
    }
}