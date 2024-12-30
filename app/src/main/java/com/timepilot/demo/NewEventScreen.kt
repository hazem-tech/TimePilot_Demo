package com.timepilot.demo

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
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
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
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
    val openDateDialog = remember { mutableStateOf(false) }
    val openCancelAlertDialog = remember { mutableStateOf(false) }
    val openDeleteAlertDialog = remember { mutableStateOf(false) }

    var initialState = remember { state }
    LaunchedEffect(state.isFullSheet, state.isPartialSheet, state.isForcedSheet) {
        initialState = state
        Log.d("NewEvent", "NewEvent: $initialState")
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fullScreenItemsShown),
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
                        initialState.eventName != state.eventName ||
                        initialState.date != state.date ||
                        initialState.anyTimeTask != state.anyTimeTask ||
                        initialState.minTime != state.minTime ||
                        initialState.maxTime != state.maxTime ||
                        initialState.trackingMode != state.trackingMode ||
                        initialState.repeat != state.repeat ||
                        initialState.allowedApps != state.allowedApps ||
                        initialState.blockedWebs != state.blockedWebs ||
                        initialState.allowedWebs != state.allowedWebs ||
                        initialState.customApps != state.customApps
                    ) {
                        openCancelAlertDialog.value = true
                        Log.d("NewEvent", "Cancel dialog shown initialState: \n$initialState, current state: \n$state")
                    } else {
                        onEvent(EventActions.HideSheet(false))
                    }
                }
                if (openCancelAlertDialog.value) {
                    SimpleAlertDialog(
                        onDismissRequest = { openCancelAlertDialog.value = false },
                        onConfirmation = {
                            openCancelAlertDialog.value = false
                            onEvent(EventActions.HideSheet(true))
                        },
                        dialogTitle = "Are you sure you want to cancel changes?",
                        mainButton = "Cancel"
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
                CustomTextField(
                    state = state,
                    onEvent = onEvent,
                    hint = "Name",
                    textStyle = if (i == 0)
                        MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface)
                    else
                        MaterialTheme.typography.headlineMedium.copy(MaterialTheme.colorScheme.onSurface),
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
                        ),
                    onDone = {
                        focusManager.clearFocus()
                        onEvent(EventActions.ShowFullSheet(state.alreadyCreatedEvent)) // not forced anymore
                    }
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
                    Button(
                        onClick = { expanded = !expanded },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.find { it.first == state.eventColor }!!.second),
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
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
                                        modifier = Modifier.size(34.dp)
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
                                    onEvent(EventActions.SetColor(color.first))
                                    expanded = false
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
                onClick = { navController.navigate("appsWebsScreen") { launchSingleTop = true } }
            ) {
                FilledTonalButton(
                    onClick = { navController.navigate("appsWebsScreen") { launchSingleTop = true } },
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
                navController.navigate("eventDuration") {
                    launchSingleTop = true
                }
                onEvent(EventActions.ShowForceFullSheet)
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
                        navController.navigate("eventRepeat") {
                            launchSingleTop = true
                        }
                        onEvent(EventActions.ShowForceFullSheet)
                    })
                )

                // TODO() I am not sure yet how to implement the start auto
//                ListItem(
//                    headlineContent = { Text("Start automatically") },
//                    leadingContent = {
//                        Icon(
//                            Icons.Outlined.PlayCircle,
//                            contentDescription = null,
//                        )
//                    },
//                    trailingContent = { Text("Never") },
//                    modifier = Modifier.clickable(onClick = {
//                        setSheet(forcedFullyExpanded)
//                    })
//                )

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
                            })
                    },
                    modifier = Modifier.clickable(onClick = { onEvent(EventActions.ChangeAnytime(!state.anyTimeTask)) }) // todo show popup explain first time
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

@Composable
fun TopButton(text: String, fontWeight: FontWeight? = null, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonColor = if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            color = buttonColor,
            fontWeight = fontWeight,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 24.dp)
        )
    }
}

@Composable
fun CustomTextField(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
    onDone: () -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle
) {
    BasicTextField(
        value = state.eventName,
        onValueChange = { onEvent(EventActions.SetEventName(it)) },
        textStyle = textStyle,
        decorationBox = { innerTextField -> // The text hint
            if (state.eventName.isEmpty()) {
                Text(
                    text = hint,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = textStyle
                )
            }
            innerTextField()
        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = modifier
    )
}

@Composable
fun SimpleAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    mainButton: String
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
    content: @Composable (Float) -> Unit
) {
    val hiddenHeight = 0.dp
    val partialHeight = 300.dp
    val fullHeight = LocalConfiguration.current.screenHeightDp.dp - 20.dp

    var sheetHeight by remember { mutableStateOf(hiddenHeight) }
    var currentHeight by remember { mutableStateOf(partialHeight) }
    val animatedHeight by animateDpAsState(targetValue = sheetHeight, label = "SheetHeightAnimation")

    LaunchedEffect(state) {
        sheetHeight = if (state.isPartialSheet) {
            partialHeight
        } else if (state.isFullSheet) {
            fullHeight
        } else if (state.isForcedSheet) {
            fullHeight
        } else {
            hiddenHeight
        }
    }

    Box {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (sheetHeight.value / fullHeight.value) / 1.6f))
            .then(
                if (state.isPartialSheet) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onEvent(EventActions.HideSheet(true))
                            sheetHeight = hiddenHeight
                        }
                    )
                } else {
                    Modifier
                }
            )
        )
        Surface(
            modifier = Modifier
                .height(animatedHeight)
                .align(Alignment.BottomCenter),
            shape = BottomSheetDefaults.ExpandedShape,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                currentHeight =
                                    if (!state.isForcedSheet)
                                        (currentHeight - delta.dp).coerceIn(
                                            hiddenHeight,
                                            fullHeight + 10.dp
                                        )
                                    else
                                        (currentHeight - delta.dp / 12).coerceIn(
                                            hiddenHeight,
                                            fullHeight + 10.dp
                                        )
                                sheetHeight = currentHeight
                            },
                            onDragStopped = {
                                sheetHeight = when {
                                    currentHeight > fullHeight * 0.7f -> {
                                        if (!state.isForcedSheet) onEvent(EventActions.ShowFullSheet(state.alreadyCreatedEvent))
                                        fullHeight
                                    }

                                    currentHeight < fullHeight * 0.3f -> {
                                        if (!state.isForcedSheet) {
                                            onEvent(EventActions.HideSheet(true))
                                            hiddenHeight
                                        } else {
                                            fullHeight
                                        }
                                    }

                                    else -> {
                                        if (!state.isForcedSheet) {
                                            onEvent(EventActions.ShowPartialSheet)
                                            partialHeight
                                        } else {
                                            fullHeight
                                        }
                                    }
                                }
                                currentHeight = sheetHeight
                            }
                        ),
                    content = { BottomSheetDefaults.DragHandle(Modifier.align(Alignment.Center)) }
                )
                content(sheetHeight.value / fullHeight.value) // to get 0 to 1 value
            }
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