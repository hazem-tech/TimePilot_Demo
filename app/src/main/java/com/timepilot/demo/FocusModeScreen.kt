package com.timepilot.demo

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.SmartDisplay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrictMode(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 7 })
    val showDialog = remember { mutableStateOf(false) }
    val enableButton = remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text("Focus mode")
                        },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Button"
                        )
                    }
                },
                actions = {
                    Button(
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = enableButton.value,
                        onClick = {
                        showDialog.value = true
                    }) {
                        Text(if (enableButton.value) "Enable" else "Ends tomorrow")
                    }
                    if (showDialog.value) {
                        FocusModeAlertDialog(
                            state = pagerState,
                            onDismissRequest = { showDialog.value = false },
                            onConfirmation = {
                                showDialog.value = false
                                enableButton.value = false
                                // TODO() enable focus mode actually in service using the selected period, you'll have 10m or 5m to disable it if it was by mistake
                            },
                            mainButton = "Enable now"
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Always running") },
                leadingContent = { Icon(Icons.Outlined.SmartDisplay, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }, // should be blue
                supportingContent = { Text("When no event is active, TimePilot will always prompt you to start one, keeping you on track") }
            )

            ListItem(
                headlineContent = { Text("Track your progress") },
                leadingContent = { Icon(Icons.Outlined.Checklist, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }, // should be green
                supportingContent = { Text("With TimePilot running all day, it helps you manage your time and track every detail automatically") }
            )

            ListItem(
                headlineContent = { Text("Limited editing") },
                leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.error) }, // should be cure red
                supportingContent = { Text("Once Focus Mode is on, changing existing events will be limited — plan your days fully before enabling it") }
            )
        }
    }
}

@Composable
fun FocusModeAlertDialog(
    state: PagerState,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    mainButton: String
) {
    AlertDialog(
        title = { Text(text = "Set focus mode for") },
        text = {
            Box {
                NumPicker(
                    state = state,
                    text = { index ->
                        if (index == 0) "today" else "${index + 1} days"
                    },
                    horizontal = true,
                    padding = PaddingValues(horizontal = 90.dp),
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
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(mainButton)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Back")
            }
        }
    )
}

@Preview
@Composable
fun FocusPreview() {
    TimePilotDemoTheme {
        StrictMode(navController = NavController(LocalContext.current))
    }
}