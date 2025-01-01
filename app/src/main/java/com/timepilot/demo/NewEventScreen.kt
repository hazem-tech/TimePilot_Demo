package com.timepilot.demo

import android.content.Context
import android.content.Context.LAUNCHER_APPS_SERVICE
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Process
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun NewEvent(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
    sheetHeight: Float,
    colors: List<Pair<String,Color>>,
    navController: NavController
) {
    val focusManager = LocalFocusManager.current
    var fullScreenItemsShown by remember { mutableFloatStateOf(0f) }
    val showAnyTimeTip = remember { mutableStateOf(false) }

    val openDateDialog = remember { mutableStateOf(false) }
    val openCancelAlertDialog = remember { mutableStateOf(false) }
    val openDeleteAlertDialog = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        // updates installed apps everytime if user deleted or installed new apps, it is being called every launch with the main activity cuz the sheet is always there
        scope.launch {
            onEvent(EventActions.UpdateInstalledApps(getInstalledApps(context).sortedBy { it.name }))
        }
        Log.d("InstalledApps", "apps being updated")
    }

    val initialState = remember { mutableStateOf(EventsStates()) } // used to check if changes happen so it will show alert when cancelling
    var saveInitialState by remember { mutableStateOf(false) }
    LaunchedEffect(state.isFullSheet, state.isPartialSheet, state.isForcedSheet) {
        if (saveInitialState) {
            initialState.value = state.copy()
            saveInitialState = false
            Log.d("NewEvent", "Initial state updated ${initialState.value.eventName}")
        } else if (!state.isFullSheet && !state.isPartialSheet && !state.isForcedSheet) {
            saveInitialState = true
        }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().alpha(fullScreenItemsShown),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = fullScreenItemsShown > 0,
                enter = expandIn(expandFrom = Alignment.Center),
                exit = shrinkOut(shrinkTowards = Alignment.Center),
            ) {
                TopButton("Cancel") {
                    focusManager.clearFocus()
                    // if nothing changed do not show dialog
                    if (
                        initialState.value.eventName != state.eventName ||
                        initialState.value.date != state.date ||
                        initialState.value.anyTimeTask != state.anyTimeTask ||
                        initialState.value.minTime != state.minTime ||
                        initialState.value.maxTime != state.maxTime ||
                        initialState.value.trackingMode != state.trackingMode ||
                        initialState.value.repeat != state.repeat ||
                        initialState.value.allowedApps != state.allowedApps ||
                        initialState.value.blockedWebs != state.blockedWebs ||
                        initialState.value.allowedWebs != state.allowedWebs ||
                        initialState.value.customAppsYt != state.customAppsYt
                    ) {
                        openCancelAlertDialog.value = true
                        Log.d("NewEvent", "Initial: ${initialState.value.eventName}, Current: ${state.eventName}")
                    } else {
                        onEvent(EventActions.HideSheet(false))
                    }
                }
                if (openCancelAlertDialog.value) {
                    SimpleAlertDialog(
                        onDismissRequest = { openCancelAlertDialog.value = false },
                        onConfirmation = {
                            openCancelAlertDialog.value = false
                            onEvent(EventActions.HideSheet(false))
                        },
                        dialogTitle = "Are you sure you want to discard your changes?",
                        mainButton = "Discard changes"
                    )
                }
            }
            AnimatedVisibility(
                visible = fullScreenItemsShown > 0,
                enter = expandIn(expandFrom = Alignment.Center),
                exit = shrinkOut(shrinkTowards = Alignment.Center),
            ) {
                TopButton("Save", FontWeight.Medium) {
                    focusManager.clearFocus()
                    onEvent(EventActions.HideSheet(true))
                }
            }
        }

        AnimatedVisibility(
            visible = fullScreenItemsShown > 0,
            enter = expandIn(expandFrom = Alignment.Center),
            exit = shrinkOut(shrinkTowards = Alignment.Center),
        ) {
            TextButton(
                onClick = { openDateDialog.value = true },
                modifier = Modifier
                    .padding(start = 10.dp, top = 3.dp)
                    .alpha(fullScreenItemsShown)
            ) {
                Text(LocalDate.parse(state.date).format(DateTimeFormatter.ofPattern("d MMM")))
            }
            if (openDateDialog.value) {
                DatePickerModal(
                    initialDate = state.date,
                    onDateSelected = { onEvent(EventActions.ChangeDate(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))))},
                    onDismiss = { openDateDialog.value = false }
                )
            }
        }

        Box(Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp)) {
            for (i in 0..1) {
                val textStyle = if (i == 0)
                    MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface)
                else
                    MaterialTheme.typography.headlineMedium.copy(MaterialTheme.colorScheme.onSurface)

                BasicTextField(
                    value = state.eventName.takeIf { it != "Untitled event" }.orEmpty(),
                    onValueChange = { onEvent(EventActions.SetEventName(it)) },
                    textStyle = textStyle,
                    decorationBox = { innerTextField -> // hint
                        if (state.eventName.takeIf { it != "Untitled event" }.orEmpty().isEmpty()) Text("Name", color = MaterialTheme.colorScheme.onSurfaceVariant, style = textStyle)
                        innerTextField()
                    },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        onEvent(EventActions.ShowFullSheet(eventID = state.alreadyCreatedEvent)) // not forced anymore
                    }),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (it.isFocused)
                                onEvent(EventActions.ShowForceFullSheet)
                            else if (state.isForcedSheet)
                                onEvent(EventActions.ShowFullSheet(state.alreadyCreatedEvent))
                        }
                        .alpha(
                            if (i == 0) {
                                if (fullScreenItemsShown < 0) (-fullScreenItemsShown + 0.3f).absoluteValue else 0f
                            } else {
                                fullScreenItemsShown
                            }
                        )
                )
            }

            if (fullScreenItemsShown > 0) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .align(Alignment.CenterEnd)
                        .alpha(fullScreenItemsShown)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    // AnimatedContent(state.eventColor, label = "ColorAnimation") { color ->
                    Button(
                        onClick = { expanded = !expanded },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.find { it.first == state.eventColor }!!.second),
                        border = BorderStroke(width = 1.dp, color = Color.Gray.copy(0.5f)),
                        content = {}
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        colors.forEach { color ->
                            DropdownMenuItem(
                                text = { Text(color.first) },
                                leadingIcon = {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(color.second)
                                    ) {
                                        if (color.first == state.eventColor) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "color chosen",
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onEvent(EventActions.SetColor(color.first))
                                }
                            )
                        }
                    }
                }
            }
        }

        ListItem(headlineContent = {
            AppsIcons(
                allowedApps = state.allowedApps.reversed(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                iconsSize = 47.dp,
                showEmpty = false,
                onClick = {
                    onEvent(EventActions.ShowForceFullSheet)
                    navController.navigate("appsWebsScreen") { launchSingleTop = true }
                }
            ) {
                FilledTonalButton(
                    onClick = {
                        onEvent(EventActions.ShowForceFullSheet)
                        navController.navigate("appsWebsScreen") { launchSingleTop = true }
                    },
                    contentPadding = if (state.allowedApps.isEmpty()) PaddingValues(horizontal = 26.dp, vertical = 11.dp) else PaddingValues(0.dp),
                    modifier = if (state.allowedApps.isEmpty()) Modifier else Modifier.size(47.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                    )
                    if (state.allowedApps.isEmpty()) Text("Add apps")
                }
            }
        })

        ListItem(
            headlineContent = { Text("Duration") },
            leadingContent = {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                )
            },
            trailingContent = { Text("${durationFormatting(state.minTime)} - ${durationFormatting(state.maxTime)}") },
            modifier = Modifier.clickable(onClick = {
                onEvent(EventActions.ShowForceFullSheet)
                navController.navigate("eventDuration") {
                    launchSingleTop = true
                }
            })
        )

        // more options section
        AnimatedVisibility(
            visible = !state.sheetHideMoreOptions,
            enter = slideInVertically(initialOffsetY = { -it })
        ) {
            Column(Modifier.alpha(fullScreenItemsShown)) {
                ListItem(
                    headlineContent = { Text("Repeat") },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.EventRepeat,
                            contentDescription = null,
                        )
                    },
                    trailingContent = {
                        Text(
                            text = if (state.repeat[0].toInt() == 0) {
                                "Never"
                            } else {
                                "every ${state.repeat[0].toInt()} ${if (state.repeat[1] == "DAILY") "days" else "weeks"}"
                            }
                        )
                    },
                    modifier = Modifier.clickable(onClick = {
                        onEvent(EventActions.ShowForceFullSheet)
                        navController.navigate("eventRepeat") {
                            launchSingleTop = true
                        }
                    })
                )

                ListItem(
                    headlineContent = { Text("Do it anytime") }, // Anytime task, better name?
                    leadingContent = {
                        Icon(
                            Icons.Outlined.MoreTime,
                            contentDescription = null,
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = state.anyTimeTask,
                            onCheckedChange = {
                                // todo show tip everytime enable it if one task from any date contains it is on, it will not show the tip ever again
                                onEvent(EventActions.ChangeAnytime(it))
                                showAnyTimeTip.value = true
                            })
                    },
                    modifier = Modifier.clickable(onClick = {
                        onEvent(EventActions.ChangeAnytime(!state.anyTimeTask))
                        showAnyTimeTip.value = true
                    })
                )

                if (state.alreadyCreatedEvent != null) {
                    ListItem(
                        headlineContent = { Text("Delete") },
                        colors = ListItemDefaults.colors(
                            headlineColor = MaterialTheme.colorScheme.error,
                            leadingIconColor = MaterialTheme.colorScheme.error
                        ),
                        leadingContent = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                            )
                        },
                        modifier = Modifier.clickable(onClick = { openDeleteAlertDialog.value = true })
                    )
                    if (openDeleteAlertDialog.value) {
                        SimpleAlertDialog(
                            onDismissRequest = { openDeleteAlertDialog.value = false },
                            onConfirmation = {
                                openDeleteAlertDialog.value = false
                                onEvent(EventActions.DeleteEvent(state.allEvent.first { it.id == state.alreadyCreatedEvent }))
                                onEvent(EventActions.HideSheet(false))
                            },
                            dialogTitle = "Are you sure you want to delete ${state.eventName}?",
                            mainButton = "Delete"
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showAnyTimeTip.value,
                    enter = scaleIn(initialScale = 0.6f, animationSpec = tween(400, easing = customEasing)) + fadeIn(),
                    exit = scaleOut(targetScale = 0.7f, animationSpec = tween(200, easing = customEasing)) + fadeOut()
                ) {
                    Box(
                        Modifier
                            .padding(20.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(18.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row {
                                Icon(
                                    Icons.Outlined.TipsAndUpdates,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(14.dp)
                                )
                                Text(
                                    "By default, tasks are completed in order. Enable Anytime to start it anytime, even if it’s not the first one.",
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            TextButton({
                                showAnyTimeTip.value = false
                            }) {
                                Text("Got it", color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(state.sheetHideMoreOptions && fullScreenItemsShown > 0) {
            ListItem(
                headlineContent = {
                    Text(
                        text = "More options",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .clickable(onClick = { onEvent(EventActions.SwitchHideMoreOptions(false)) })
                    .alpha(fullScreenItemsShown)
            )
        }

        LaunchedEffect(sheetHeight) {
            fullScreenItemsShown = (sheetHeight - 0.6f) * 5
        }

        // handling back button
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        if (currentBackStackEntry?.destination?.route == "eventSheet") {
            if (state.isPartialSheet)
                BackHandler { onEvent(EventActions.HideSheet(true)) }
            else if (state.isFullSheet)
                BackHandler { onEvent(EventActions.ShowPartialSheet) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    initialDate: String,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.parse(initialDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val newDate = datePickerState.selectedDateMillis
                if (newDate != null) {
                    onDateSelected(newDate)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
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

@Preview(showBackground = true)
@Composable
fun SheetPreview() {
    TimePilotDemoTheme {
        NewEvent(
            state = EventsStates(),
            onEvent = { },
            sheetHeight = 800f,
            colors = listOf(),
            navController = rememberNavController()
        )
    }
}