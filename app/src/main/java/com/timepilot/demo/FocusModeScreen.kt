package com.timepilot.demo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrictMode(navController: NavController) {
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
                    Button(modifier = Modifier.padding(end = 8.dp), onClick = {
                        // todo
                    }) {
                        Text("Enable for today")
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text("Always running") },
                leadingContent = { Icon(Icons.Outlined.PlayCircle, contentDescription = null) },
                supportingContent = { Text("When focus mode is enabled, TimePilot will prompt you to start an event if none is active, making sure you always are aware of the tasks you have") }
            )

            ListItem(
                headlineContent = { Text("Limited editing") },
                leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                supportingContent = { Text("Make sure before enabling focus mode is to be fully planned the day ahead, once it is enabled some changes to today's events will be disabled") }
            )

            ListItem(
                headlineContent = { Text("Track your progress") },
                leadingContent = { Icon(Icons.Outlined.CheckCircleOutline, contentDescription = null) },
                supportingContent = { Text("Having TimePilot running all day will help you manage your time and track every detail of the day automatically") }
            )
        }
    }
}

@Preview
@Composable
fun FocusPreview() {
    TimePilotDemoTheme {
        StrictMode(navController = NavController(LocalContext.current))
    }
}