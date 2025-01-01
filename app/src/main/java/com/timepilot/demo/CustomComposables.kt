package com.timepilot.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

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
                Text("Go back")
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
fun AppsSearchBar(
    textState: MutableState<String>,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    menuSelectAllOnClick: (() -> Unit)?,
    menuUnselectAllOnClick: () -> Unit,
    menuSortOnClick: (() -> Unit)?
) {
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }

    TextField(
        value = textState.value,
        placeholder = { Text("Search") },
        singleLine = true,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.padding(start = 16.dp))
        },
        trailingIcon = {
            AnimatedVisibility(visible = textState.value.isEmpty()) {
                Box {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (menuSelectAllOnClick != null) "Select all" else "Unselect all") },
                            onClick = menuSelectAllOnClick ?: menuUnselectAllOnClick
                        )
                        DropdownMenuItem(
                            text = { Text(if (menuSortOnClick != null) "Sort Alphabetically" else "Sort by app usage") },
                            onClick = {
                                // todo sort by app usage
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(visible = textState.value.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Empty search text",
                    modifier = Modifier
                        .clickable {
                            textState.value = ""
                            focusManager.clearFocus()
                        }
                        .padding(16.dp)
                )
            }
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = if (!isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.surfaceContainerLow,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = modifier.clip(RoundedCornerShape(50.dp))
    )
}

// todo i am not sure if the horizontal and vertical pickers work on large screens like tablet
@Composable
fun NumPicker(
    modifier: Modifier = Modifier,
    state: PagerState,
    text: (Int) -> String,
    horizontal: Boolean,
    padding: PaddingValues,
    textHeight: Dp
) {
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val pagerContent: @Composable PagerScope.(Int) -> Unit = { index ->
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val pageOffset =
                        ((state.currentPage - index) + state.currentPageOffsetFraction).absoluteValue
                    // Set the item alpha based on the distance from the center
                    val percentFromCenter = 1.0f - (pageOffset / (5f / 2f))
                    val opacity = 0.25f + (percentFromCenter * 0.75f).coerceIn(0f, 1f)
                    alpha = opacity
                }
                .clickable(
                    interactionSource = mutableInteractionSource,
                    indication = null,
                    enabled = true,
                ) {
                    scope.launch {
                        state.animateScrollToPage(index)
                    }
                }
        ) {
            Text(
                text = text(index),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .height(textHeight)
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }

    if (horizontal) {
        HorizontalPager(
            state = state,
            contentPadding = padding,
            modifier = modifier,
            pageContent = pagerContent
        )
    } else {
        VerticalPager(
            state = state,
            contentPadding = padding,
            modifier = modifier,
            pageContent = pagerContent
        )
    }
}