package com.timepilot.demo

import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import android.provider.Settings.EXTRA_APP_PACKAGE
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var alwaysEnable by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val disableUserEditing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Button"
                        )
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ListItem(
                headlineContent = { Text( "Create account (soon)") },
                leadingContent = {
                    FilledIconButton(
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = contentColorFor(MaterialTheme.colorScheme.tertiary)
                        ),
                        modifier = Modifier.size(45.dp),
                        onClick = {} // it will refresh the image from google acc not selectable
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Face,
                            contentDescription = "Profile Picture",
                        )
                    }
                },
                modifier = Modifier.clickable(onClick = {
                    // TODO()
                })
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text( "Focus Mode") },
                leadingContent = {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                    )
                },
                trailingContent = { Switch(checked = alwaysEnable, onCheckedChange = {
                    alwaysEnable = !alwaysEnable
                    navController.navigate("strictModeScreen") {
                        launchSingleTop = true
                    }
                }) },
                modifier = Modifier.clickable(onClick = {
                    navController.navigate("strictModeScreen") {
                        launchSingleTop = true
                    }
                })
            )

            HorizontalDivider()

            ListItem(
                headlineContent = { Text( "Always allowed apps") },
                leadingContent = {
                    Icon(
                        Icons.Outlined.Apps,
                        contentDescription = null,
                    )
                },
                trailingContent = { Text("0 selected") }, // todo
                modifier = Modifier.clickable(onClick = {
                    navController.navigate("alwaysAllowedApps") {
                        launchSingleTop = true
                    }
                })
            )

            HorizontalDivider()

            if (!disableUserEditing) {
                ListItem(
                    headlineContent = { Text("Manage notification") },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.Notifications,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable(onClick = {
                        val intent = Intent(ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(EXTRA_APP_PACKAGE, context.packageName)
                        context.startActivity(intent)
                    })
                )
            }

            ListItem(
                headlineContent = { Text( "Rate us") },
                leadingContent = {
                    Icon(
                        Icons.Outlined.StarBorder,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://timepilot.org/RatePlayStore"))
                    context.startActivity(intent)
                })
            )

            ListItem(
                headlineContent = { Text( "Premium") },
                leadingContent = {
                    Icon(
                        Icons.Outlined.AutoGraph,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://timepilot.org/Subscribe"))
                    context.startActivity(intent)
                })
            )

            ListItem(
                headlineContent = { Text( "Help & feedback") },
                leadingContent = {
                    Icon(
                        Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://timepilot.org/HelpFeedback"))
                    context.startActivity(intent)
                })
            )

            if (disableUserEditing) {
                Icon(
                    Icons.Outlined.TipsAndUpdates,
                    contentDescription = null,
                )
                Text("Some actions are not available because there is a current active event")
                Text("Helps focus by prompting you to start tasks when none are active. Disables editing, deleting, or reordering today’s already created tasks")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    TimePilotDemoTheme {
        SettingsScreen(rememberNavController())
    }
}