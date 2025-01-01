package com.timepilot.demo

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@Composable
fun WebsScreen(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
) {
    // todo if allowed apps does not have browser or youtube, it will show a popup that all browsers are added
    val suggestionChips = listOf("Block all websites", "TikTok", "YouTube", "Instagram")
    val focusManager = LocalFocusManager.current // for when you tap on a suggestion chip

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            AddItemButton("Blocked websites") {
                if (state.blockedWebs.getOrNull(0)?.text?.isBlank() != true) {
                    onEvent(EventActions.ChangeBlockedWebs(
                        listOf(UniqueString("")) + state.blockedWebs
                    ))
                }
            }
        }

        itemsIndexed(
            items = if (!state.blockedWebs.any { it.text == "Blockallwebsites" }) {
                state.blockedWebs
            } else {
                state.blockedWebs.filter { it.text == "Blockallwebsites" }
            },
            key = { _, item -> item.id }
        ) { index, item ->
            WebsiteItem(
                itemName = if (item.text != "Blockallwebsites") item.text else "Block all websites",
                onValueChange = { newText ->
                    onEvent(EventActions.ChangeBlockedWebs(
                        state.blockedWebs.toMutableList().apply {
                            this[index] = state.blockedWebs[index].copy(text = newText)
                        }
                    ))
                },
                hint = "Type blocked URL...",
                checkDuplicatedItem = {
                    onEvent(EventActions.ChangeBlockedWebs(
                        filterList(state.blockedWebs, state.allowedWebs)
                    ))
                    onEvent(EventActions.ChangeAllowedWebs(
                        filterList(state.allowedWebs, state.blockedWebs)
                    ))
                },
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = tween(durationMillis = 300),
                        fadeOutSpec = tween(durationMillis = 300),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            )

            if (index == 0 && item.text.isBlank()) {
                Column {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(suggestionChips) { suggestionName ->
                            SuggestionChip(
                                onClick = {
                                    onEvent(EventActions.ChangeBlockedWebs(
                                        state.blockedWebs.mapIndexed { index, item ->
                                            if (index == 0) item.copy(text = suggestionName) else item
                                        }
                                    ))
                                    focusManager.clearFocus()
                                },
                                label = { Text(suggestionName) }
                            )
                        }
                    }
                }
            }
        }

        item {
            if (state.blockedWebs.any { it.text.isNotBlank() }) {
                Column {
                    HorizontalDivider()
                    AddItemButton("Allowed websites") {
                        if (state.allowedWebs.getOrNull(0)?.text?.isBlank() != true) {
                            onEvent(EventActions.ChangeAllowedWebs(
                                listOf(UniqueString("")) + state.allowedWebs
                            ))
                        }
                    }
                }
            }
        }

        if (state.blockedWebs.any { it.text.isNotBlank() }) {
            itemsIndexed(
                items = state.allowedWebs,
                key = { _, item -> item.id }
            ) { index, item ->
                WebsiteItem(
                    itemName = item.text,
                    onValueChange = { newText ->
                        onEvent(EventActions.ChangeAllowedWebs(
                            state.allowedWebs.toMutableList().apply {
                                this[index] = state.allowedWebs[index].copy(text = newText)
                            }
                        ))
                    },
                    hint = "Type allowed URL...",
                    checkDuplicatedItem = {
                        onEvent(EventActions.ChangeBlockedWebs(
                            filterList(state.blockedWebs, state.allowedWebs)
                        ))
                        onEvent(EventActions.ChangeAllowedWebs(
                            filterList(state.allowedWebs, state.blockedWebs)
                        ))
                    },
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(durationMillis = 300),
                            fadeOutSpec = tween(durationMillis = 300),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                )
            }
        }

        item {
            AnimatedVisibility(state.blockedWebs.isEmpty()) {
                InfoText("You can add exceptions, allowing specific pages in a blocked website", buttonOnClick = {})
            }
        }
    }
}

@Composable
fun AddItemButton(text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text, color = MaterialTheme.colorScheme.primary) },
        leadingContent = {
            Icon(Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        modifier = Modifier.clickable{ onClick() }
    )
}

@Composable
fun WebsiteItem(
    modifier: Modifier = Modifier,
    itemName: String,
    onValueChange: (String) -> Unit,
    hint: String,
    checkDuplicatedItem: () -> Unit
) {
    val focusRequester = FocusRequester()
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(itemName, isFocused) {
        if (itemName == "" && !isFocused) {
            focusRequester.requestFocus()
        } else if (!isFocused) {
            checkDuplicatedItem()
        }
    }

    ListItem(
        headlineContent = {
            BasicTextField(
                value = itemName,
                onValueChange = { onValueChange(it) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    if (itemName.isEmpty()) {
                        Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    }
                    innerTextField()
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        isFocused = false
                    }
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        isFocused = it.isFocused
                    }
            )
        },
        leadingContent = {
            AnimatedVisibility(!isFocused && itemName.isNotEmpty()) {
                Checkbox(
                    checked = true,
                    onCheckedChange = {
                        onValueChange(" ")
                        isFocused = false
                    }
                )
            }
        },
        trailingContent = {
            AnimatedVisibility(isFocused && itemName.isNotEmpty()) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Save new keyword",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier // order matters
                        .clip(CircleShape)
                        .clickable {
                            focusManager.clearFocus()
                            isFocused = false
                        }
                        .padding(12.dp)
                )
            }
        },
        modifier = modifier
    )
}

fun filterList(list1: List<UniqueString>, list2: List<UniqueString>): List<UniqueString> {
    val regex = Regex("(?i)(\\s|https?://|www\\.|/*\$)")
    return list1
        .filter { it.text.isNotBlank() }  // Remove items with blank text
        .filterNot { item -> list2.any { it.text == item.text } }  // Exclude items already in list2
        .distinctBy { it.text }  // Ensure distinct items based on text
        .map { it.copy(text = regex.replace(it.text, "")) } // apply regex
}

@Composable
fun CustomAppScreen(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
) {
    val pm = LocalContext.current.packageManager

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            ListItem(
                headlineContent = {
                    Text("YouTube")
                },
                leadingContent = {
                    Image(
                        bitmap = pm.getApplicationIcon("com.google.android.youtube").toBitmap().asImageBitmap(), // painterResource(id = R.drawable.ic_launcher_background)
                        contentDescription = null,
                        Modifier.height(40.dp)
                    )
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Block Shorts") },
                trailingContent = {
                    Switch(
                        checked = state.customAppsYt.any { it.text == "BLOCK_SHORTS" },
                        onCheckedChange = { updateCustomAppsList("BLOCK_SHORTS", state.customAppsYt, onEvent) }
                    )
                },
                modifier = Modifier.clickable { updateCustomAppsList("BLOCK_SHORTS", state.customAppsYt, onEvent) }.padding(horizontal = 12.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Block all videos") },
                trailingContent = {
                    Switch(
                        checked = state.customAppsYt.any { it.text == "BLOCK_ALL" },
                        onCheckedChange = { updateCustomAppsList("BLOCK_ALL", state.customAppsYt, onEvent) }
                    )
                },
                modifier = Modifier.clickable { updateCustomAppsList("BLOCK_ALL", state.customAppsYt, onEvent) }.padding(horizontal = 12.dp)
            )
        }

        item {
            AnimatedVisibility(
                visible = state.customAppsYt.any { it.text == "BLOCK_ALL" },
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                AddItemButton("Exception keywords") {
                    if (state.customAppsYt.filter { it.text != "BLOCK_ALL" && it.text != "BLOCK_SHORTS" }.getOrNull(0)?.text?.isBlank() != true) {
                        onEvent(EventActions.ChangeCustomApps(
                            listOf(UniqueString("")) + state.customAppsYt
                        ))
                    }
                }
            }
        }

        if (state.customAppsYt.any { it.text == "BLOCK_ALL" }) { // only show lists if i enable block videos
            itemsIndexed(
                items = state.customAppsYt.filter { it.text != "BLOCK_ALL" && it.text != "BLOCK_SHORTS" },
                key = { _, item -> item.id }
            ) { index, item ->
                WebsiteItem(
                    itemName = item.text,
                    onValueChange = { newText ->
                        onEvent(EventActions.ChangeCustomApps(
                            state.customAppsYt.toMutableList().apply {
                                this[index] = state.customAppsYt[index].copy(text = newText)
                            }
                        ))
                    },
                    hint = "Type video keyword or channel...",
                    checkDuplicatedItem = {
                        onEvent(EventActions.ChangeCustomApps(
                            state.customAppsYt.filter { it.text.isNotBlank() }.distinctBy { it.text }
                        ))
                        Log.d("CustomAppScreen", "checkDuplicatedItem: ${state.customAppsYt}")
                    },
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(durationMillis = 300),
                            fadeOutSpec = tween(durationMillis = 300),
                            placementSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                )
            }
        }
    }
}

fun updateCustomAppsList(text: String, list: List<UniqueString>, onEvent: (EventActions) -> Unit) {
    onEvent(EventActions.ChangeCustomApps(
        if (list.find { it.text == text } != null) {
            list.toMutableList().apply { this.removeIf { it.text == text } }
        } else {
            list.toMutableList().apply { this.add(UniqueString(text = text)) }
        }
    ))
}

@Preview
@Composable
fun WebPreview() {
    TimePilotDemoTheme {
        WebsScreen(EventsStates(), onEvent = {})
    }
}

@Preview
@Composable
fun CustomScreenPreview() {
    TimePilotDemoTheme {
        CustomAppScreen(EventsStates(), onEvent = {})
    }
}