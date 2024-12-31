package com.timepilot.demo

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
import androidx.compose.runtime.mutableStateListOf
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
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }
    // todo if allowed apps does not have browser or youtube, it will show a popup that all browsers are added
    val suggestionChips = listOf("Block all websites", "TikTok", "YouTube", "Instagram")

    val regex = Regex("(?i)(\\s|https?://|www\\.|/*\$)")
    LaunchedEffect(isFocused) {
        if (!isFocused) {
            onEvent(EventActions.ChangeBlockedWebs(
                state.blockedWebs.filter { it.isNotBlank() }.filter { it !in state.allowedWebs }.distinct().map { item ->
                    regex.replace(item, "")
                }
            ))
            onEvent(EventActions.ChangeAllowedApps(
                state.allowedApps.filter { it.isNotBlank() }.filter { it !in state.blockedWebs }.distinct().map { item ->
                    regex.replace(item, "")
                }
            ))
        }
    }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            AddItemButton("Blocked websites") {
                // if list is empty, will return null, so it will execute, if first item is not blank, it will return false we
                // asked if its blank, it is not so false, so it will execute, now the only time it is true is if not blank, but we will execute if not true, if true we won't execute
                if (state.blockedWebs.getOrNull(0)?.isBlank() != true) {
                    onEvent(EventActions.ChangeBlockedWebs(listOf("") + state.blockedWebs))
                }
            }
        }

        itemsIndexed(state.blockedWebs) { index, web ->
            AnimatedVisibility(state.blockedWebs.isNotEmpty()) {
                WebsiteItem(
                    itemName = web,
                    onValueChange = {
                        onEvent(EventActions.ChangeBlockedWebs(state.blockedWebs.toMutableList().apply { this[index] = it }))
                    },
                    hint = "Type blocked URL...",
                    isFocused = isFocused,
                    changeFocus = { isFocused = it }
                )
            }

            AnimatedVisibility(
                visible = web.isBlank(),
                enter = slideInVertically(),
                exit = slideOutVertically(targetOffsetY = { it / 2 })
            ) {
                Column {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(suggestionChips) { name ->
                            SuggestionChip(
                                onClick = {
                                    onEvent(EventActions.ChangeBlockedWebs(
                                        state.blockedWebs.toMutableList().apply { this[0] = name }
                                    ))
                                    focusManager.clearFocus()
                                },
                                label = { Text(name) }
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        item {
            AnimatedVisibility(state.blockedWebs.any { it.isNotBlank() }) {
                Column {
                    HorizontalDivider()
                    AddItemButton("Allowed websites") {
                        if (state.allowedWebs.getOrNull(0)?.isBlank() != true) {
                            onEvent(EventActions.ChangeAllowedWebs(listOf("") + state.allowedWebs))
                        }
                    }
                }
            }
        }

        itemsIndexed(state.allowedWebs) { index, web ->
            AnimatedVisibility(true) {
                WebsiteItem(
                    itemName = web,
                    onValueChange = {
                        onEvent(EventActions.ChangeAllowedWebs(state.allowedWebs.toMutableList().apply { this[index] = it }))
                    },
                    hint = "Type exception URL...",
                    isFocused = isFocused,
                    changeFocus = { isFocused = it }
                )
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
    isFocused: Boolean,
    changeFocus: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val beingEdited = remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            BasicTextField(
                value = itemName,
                onValueChange = {
                    onValueChange(it)
                    beingEdited.value = true
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                decorationBox = { innerTextField ->
                    if (itemName.isEmpty()) {
                        Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
                    }
                    innerTextField()
                },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        beingEdited.value = false
                    }
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        changeFocus(it.isFocused)
                        beingEdited.value = false
                    }
            )
        },
        leadingContent = {
            AnimatedVisibility(!beingEdited.value && itemName.isNotEmpty()) {
                Checkbox(
                    checked = true,
                    onCheckedChange = {
                        // to trigger change in LaunchedEffect
                        changeFocus(true)
                        onValueChange("")
                        changeFocus(false)
                    }
                )
            }
        },
        trailingContent = {
            AnimatedVisibility(beingEdited.value && isFocused && itemName.isNotEmpty()) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Save new keyword",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier // order matters
                        .clip(CircleShape)
                        .clickable { focusManager.clearFocus() }
                        .padding(16.dp)
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun CustomAppScreen(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
) {
    // the goal is to have unique ones like white writing even unique and be stable not changing depending on the index or smth to make animations correct
    val ytList = remember { mutableStateListOf<UniqueString>() }

    var isFocused by remember { mutableStateOf(false) }
    val pm = LocalContext.current.packageManager

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            onEvent(EventActions.ChangeCustomApps(state.customAppsYt.filter { it.isNotBlank() }.distinct()))
            //ytList.removeAll(ytList)
            //ytList.addAll(state.customAppsYt.map { UniqueString(it) })
        }
    }

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
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Block Shorts") },
                trailingContent = {
                    Switch(
                        checked = state.customAppsYt.contains("BLOCK_SHORTS"),
                        onCheckedChange = {
                            onEvent(EventActions.ChangeCustomApps(state.customAppsYt.toMutableList().apply {
                                if (!state.customAppsYt.contains("BLOCK_SHORTS")) this.add("BLOCK_SHORTS") else this.remove("BLOCK_SHORTS")
                            }))
                        }
                    )
                },
                modifier = Modifier.clickable {
                    onEvent(EventActions.ChangeCustomApps(state.customAppsYt.toMutableList().apply {
                        if (!state.customAppsYt.contains("BLOCK_SHORTS")) this.add("BLOCK_SHORTS") else this.remove("BLOCK_SHORTS")
                    }))
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Block all videos") },
                trailingContent = {
                    Switch(
                        checked = state.customAppsYt.contains("BLOCK_ALL"),
                        onCheckedChange = {
                            onEvent(EventActions.ChangeCustomApps(state.customAppsYt.toMutableList().apply {
                                if (!state.customAppsYt.contains("BLOCK_ALL"))
                                    this.add("BLOCK_ALL")
                                else
                                    this.remove("BLOCK_ALL")
                            }))
                        }
                    )
                },
                modifier = Modifier.clickable {
                    onEvent(EventActions.ChangeCustomApps(state.customAppsYt.toMutableList().apply {
                        if (!state.customAppsYt.contains("BLOCK_ALL")) this.add("BLOCK_ALL") else this.remove("BLOCK_ALL")
                    }))
                }
            )
        }

        item {
            AnimatedVisibility(
                visible = state.customAppsYt.any { it == "BLOCK_ALL" },
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                AddItemButton("Exception keywords") {
                    if (ytList.filter { it.value != "BLOCK_ALL" && it.value != "BLOCK_SHORTS" }.getOrNull(0)?.value?.isBlank() != true) {
                        ytList.add(0, UniqueString(""))
                        //onEvent(EventActions.ChangeCustomApps(ytList.map { it.value }))
                    }
                }
            }
        }

        if(state.customAppsYt.any { it == "BLOCK_ALL" }) {
            itemsIndexed(
                items = ytList.filter { it.value != "BLOCK_ALL" && it.value != "BLOCK_SHORTS" },
                key = { _, item -> item.id }
            ) { index, item ->
                WebsiteItem(
                    itemName = item.value,
                    onValueChange = { newText ->
                        ytList[index] = ytList[index].copy(value = newText)
                        //onEvent(EventActions.ChangeCustomApps(ytList.map { it.value }))
                    },
                    hint = "Type exception video keyword or channel...",
                    isFocused = isFocused,
                    changeFocus = { isFocused = it },
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