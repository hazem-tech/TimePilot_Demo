package com.timepilot.demo

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
fun EventDuration(eventName: String, navController: NavController) {
    val minTimes by remember { mutableStateOf(
        listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 90, 120,
            150, 180, 210, 240, 300, 360, 420, 480, 540, 600, 660, 720))
    }
    var maxTimes by remember { mutableStateOf(minTimes.drop(1)) } // max can't be zero
    var maxCurrentValue: Int

    val minPagerState = rememberPagerState(pageCount = { minTimes.size }, initialPage = 2) // TODO
    val maxPagerState = rememberPagerState(pageCount = { maxTimes.size }, initialPage = 5) // TODO

    var eventMin = 0
    var eventMax = 0

    Column(Modifier.fillMaxSize()) {
        LargeTopAppBar(
            title = { Text("$eventName duration") },
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
                        val min = num % 60
                        val hrs = num / 60
                        when {
                            hrs == 0 -> "$min min"
                            min == 0 -> if (hrs == 1) "1 hr" else "$hrs hrs"
                            else -> "${hrs}h ${min}m"
                        }
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

            LaunchedEffect(minPagerState) {
                snapshotFlow { minPagerState.settledPage }.collect { page ->
                    eventMin = page
                    // todo actually affect data

                    maxCurrentValue = maxTimes[maxPagerState.currentPage]
                    maxTimes = if (page > 0) minTimes.drop(page) else minTimes.drop(1) // max can't be zero
                    maxPagerState.scrollToPage(
                        if (maxTimes[0] <= maxCurrentValue) maxTimes.indexOf(maxCurrentValue) else 0
                    )
                    Log.d("minTimes", "$maxCurrentValue")
                }
            }

            LaunchedEffect(maxPagerState) {
                snapshotFlow { maxPagerState.settledPage }.collect { page ->
                    eventMax = page
                }
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
            trailingContent = { Text("Countdown") },
            modifier = Modifier.clickable(onClick = {
                navController.navigate("eventTrackingScreen") {
                    launchSingleTop = true
                }
            })
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTracking(navController: NavController) {
    var selectedOption by remember { mutableIntStateOf(1) } // TODO() actually affect event
    val options = listOf(
        Triple("App usage", "Ensures you spend the specified duration actively using the selected apps", Icons.Outlined.Leaderboard),
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

        options.forEachIndexed { index, (headline, description, icon) ->
            ListItem(
                headlineContent = { Text(headline) },
                leadingContent = { Icon(icon, contentDescription = null) },
                supportingContent = { Text(description) },
                trailingContent = { RadioButton(selected = selectedOption == index, onClick = { selectedOption = index }) },
                modifier = Modifier.clickable(onClick = { selectedOption = index })
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DurationPreview() {
    TimePilotDemoTheme {
        EventDuration("Hello", rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun TrackingPreview() {
    TimePilotDemoTheme {
        EventTracking(rememberNavController())
    }
}