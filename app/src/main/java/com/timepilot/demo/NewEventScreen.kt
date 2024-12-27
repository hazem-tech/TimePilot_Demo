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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun NewEvent(mode: Int, setSheet: (Int) -> Unit, sheetHeight: Float, newOne: Boolean, navController: NavController) {
    var eventDate by remember { mutableStateOf(SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(System.currentTimeMillis()))) }
    var anyTimeTask by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    var hideMoreOptions by remember { mutableStateOf(true) }
    var fullScreenItemsShown by remember { mutableFloatStateOf(0f) }
    var doNotSave by remember { mutableStateOf(false) }
    var openDateDialog by remember { mutableStateOf(false) }

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
                    // TODO() if current event != a brand new event && !doNotSave, then show confirmation dialog before making it true
                    doNotSave = true
                    focusManager.clearFocus()
                    setSheet(hidden)
                }
            }
            AnimatedVisibility(
                visible = fullScreenItemsShown > 0,
                enter = expandIn(expandFrom = Alignment.Center),
                exit = shrinkOut(shrinkTowards = Alignment.Center),
            ) {
                TopButton("Save", FontWeight.Medium) {
                    focusManager.clearFocus()
                    setSheet(hidden)
                }
            }
        }

        AnimatedVisibility(
            visible = fullScreenItemsShown > 0,
            enter = expandIn(expandFrom = Alignment.Center),
            exit = shrinkOut(shrinkTowards = Alignment.Center),
        ) {
            TextButton(
                onClick = { openDateDialog = true },
                modifier = Modifier
                    .padding(start = 10.dp, top = 3.dp)
                    .alpha(fullScreenItemsShown)
            ) {
                Text(eventDate)
            }
            if (openDateDialog) {
                DatePickerModal(
                    initialSelectedDate = LocalDate.now(),
                    onDateSelected = { eventDate = it},
                    onDismiss = { openDateDialog = false }
                )
            }
        }

        Box(Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp)) {
            for (i in 0..1) {
                CustomTextField(
                    initialText = "",
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
                                setSheet(forcedFullyExpanded)
                            else if (mode == forcedFullyExpanded)
                                setSheet(fullyExpanded)
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
                        setSheet(fullyExpanded) // not forced anymore
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
                    val colorsOptions = listOf(
                        Pair("Main", MaterialTheme.colorScheme.primaryContainer),
                        Pair("Red", Color(0xFFFFD8D8)),
                        Pair("Green", Color(0xFFD3E8E9)),
                        Pair("Blue", Color(0xFFD8E3FF)),
                        Pair("Yellow", Color(0xFFEAE4D5)),
                        Pair("Pink", Color(0xFFFFD8ED)),
                        Pair("Purple", Color(0xFFF1D8FF))
                    )
                    Button(
                        onClick = { expanded = !expanded },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        border = BorderStroke(width = 1.dp, color = Color.Gray),
                        content = {}
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.background)
                    ) {
                        colorsOptions.forEach { (colorName, color) ->
                            DropdownMenuItem(
                                text = { Text(colorName) },
                                leadingIcon = {
                                    Box(
                                        Modifier
                                            .size(25.dp)
                                            .clip(CircleShape)
                                            .background(color))
                                },
                                onClick = {
                                    expanded = false
                                    // TODO()
                                }
                            )
                        }
                    }
                }
            }
        }

        // TODO() change look when there are apps selected, maybe show max of 5?
        ListItem(headlineContent = {
            FilledTonalButton(
                onClick = {
                    navController.navigate("appsWebsScreen") {
                        launchSingleTop = true
                    }
                    setSheet(forcedFullyExpanded)
                },
                contentPadding = PaddingValues(horizontal = 26.dp, vertical = 11.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                )
                Text("Add apps")
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
            trailingContent = { Text("15m - 30m") },
            modifier = Modifier.clickable(onClick = {
                navController.navigate("eventDuration") {
                    launchSingleTop = true
                }
                setSheet(forcedFullyExpanded)
            })
        )

        // more options section
        AnimatedVisibility(
            visible = !hideMoreOptions,
            enter = slideInVertically(initialOffsetY = { -it })
        ) {
            Column(Modifier.alpha(fullScreenItemsShown)) {
                HorizontalDivider()

                ListItem(
                    headlineContent = { Text("Repeat") },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.EventRepeat,
                            contentDescription = null,
                        )
                    },
                    trailingContent = { Text("Never") },
                    modifier = Modifier.clickable(onClick = {
                        navController.navigate("eventRepeat") {
                            launchSingleTop = true
                        }
                        setSheet(forcedFullyExpanded)
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
                    headlineContent = { Text("Anytime task") },
                    leadingContent = {
                        Icon(
                            Icons.Outlined.MoreTime,
                            contentDescription = null,
                        )
                    },
                    trailingContent = {
                        Switch(checked = anyTimeTask, onCheckedChange = { anyTimeTask = !anyTimeTask })
                    },
                    modifier = Modifier.clickable(onClick = { anyTimeTask = !anyTimeTask }) // todo show popup explain first time
                )

                if (!newOne) {
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
                        modifier = Modifier.clickable(onClick = {
                            // TODO() delete event
                        })
                    )
                }
            }
        }
        AnimatedVisibility(hideMoreOptions && fullScreenItemsShown > 0) {
            ListItem(
                headlineContent = {
                    Text(
                        text = "More options",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .clickable(onClick = { hideMoreOptions = false })
                    .alpha(fullScreenItemsShown)
            )
        }

        LaunchedEffect(mode) {
            if (mode == hidden) {
                doNotSave = false
                // TODO() if current event != a brand new event && !doNotSave, then save it
            }
        }
        LaunchedEffect(sheetHeight) {
            if (sheetHeight < 0.6f) {
                hideMoreOptions = true
            }
            fullScreenItemsShown = (sheetHeight - 0.6f) * 5
        }

        // handling back button
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        if (currentBackStackEntry?.destination?.route == "eventSheet") {
            if (mode == partiallyShown)
                BackHandler { setSheet(hidden)}
            else if (mode == fullyExpanded)
                BackHandler { setSheet(partiallyShown)}
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
fun CustomTextField(initialText: String, textStyle: TextStyle, hint: String, modifier: Modifier = Modifier, onDone: () -> Unit) {
    var eventName by remember { mutableStateOf(TextFieldValue(initialText)) }
    BasicTextField(
        value = eventName,
        onValueChange = { newText -> eventName = newText },
        textStyle = textStyle,
        decorationBox = { innerTextField -> // The text hint
            if (eventName.text.isEmpty()) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    mode: Int, // 0 is hidden. 1 is partial. 2 is full
    changeMode: (Int) -> Unit,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable (Float) -> Unit
) {
    val hiddenHeight = 0.dp
    val partialHeight = 300.dp
    val fullHeight = LocalConfiguration.current.screenHeightDp.dp - 20.dp

    var sheetHeight by remember { mutableStateOf(hiddenHeight) }
    var currentHeight by remember { mutableStateOf(partialHeight) }
    val animatedHeight by animateDpAsState(targetValue = sheetHeight, label = "SheetHeightAnimation")

    LaunchedEffect(mode) {
        sheetHeight = when (mode) {
            partiallyShown -> partialHeight
            fullyExpanded -> fullHeight
            forcedFullyExpanded -> fullHeight
            else -> hiddenHeight
        }
        Log.d("FUckMode", mode.toString())
    }

    Box {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (sheetHeight.value / fullHeight.value) / 1.6f))
            .then(
                if (mode == 1) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            changeMode(hidden)
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
            color = containerColor,
            contentColor = contentColor,
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                currentHeight =
                                    if (mode != forcedFullyExpanded)
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
                                        if (mode != forcedFullyExpanded) changeMode(fullyExpanded)
                                        fullHeight
                                    }

                                    currentHeight < fullHeight * 0.3f -> {
                                        if (mode != forcedFullyExpanded) {
                                            changeMode(hidden)
                                            hiddenHeight
                                        } else {
                                            fullHeight
                                        }
                                    }

                                    else -> {
                                        if (mode != forcedFullyExpanded) {
                                            changeMode(partiallyShown)
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
    initialSelectedDate: LocalDate,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDate.atStartOfDay()
        .toInstant(ZoneOffset.UTC)
        .toEpochMilli())

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val newDate = datePickerState.selectedDateMillis
                if (newDate != null) {
                    onDateSelected(SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(newDate)))
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
        NewEvent(fullyExpanded, { }, 800f, true, rememberNavController())
    }
}


const val hidden = 0
const val partiallyShown = 1
const val fullyExpanded = 2
// const val disablePartialExpanded = 3
const val forcedFullyExpanded = 4