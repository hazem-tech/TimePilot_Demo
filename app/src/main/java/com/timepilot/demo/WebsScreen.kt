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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
    // the goal is to have unique ones like white writing even unique and be stable not changing depending on the index or smth to make animations correct
    val blockedList = remember { mutableStateListOf<UniqueString>() }
    val allowedList = remember { mutableStateListOf<UniqueString>() }
    blockedList.addAll(state.blockedWebs.map { UniqueString(text = it) })
    allowedList.addAll(state.allowedWebs.map { UniqueString(text = it) })
    val suggestionChips = listOf("Block all websites", "TikTok", "YouTube", "Instagram")

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            AddItemButton("Blocked websites") {
                if (blockedList.getOrNull(0)?.text?.isBlank() != true) {
                    blockedList.add(0, UniqueString(""))
                }
            }
        }

        itemsIndexed(
            items = blockedList,
            key = { _, item -> item.id }
        ) { index, item ->
            WebsiteItem(
                itemName = item.text,
                onValueChange = { newText ->
                    blockedList[index] = blockedList[index].copy(text = newText)
                },
                hint = "Type blocked URL...",
                checkDuplicatedItem = {
                    val uniqueBlockedList = filterList(blockedList, allowedList)
                    blockedList.clear()
                    blockedList.addAll(uniqueBlockedList)

                    val uniqueAllowedList = filterList(allowedList, blockedList)
                    allowedList.clear()
                    allowedList.addAll(uniqueAllowedList)

                    onEvent(EventActions.ChangeBlockedWebs(blockedList.map { it.text }))
                    onEvent(EventActions.ChangeAllowedWebs(allowedList.map { it.text }))
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

            if (item.text.isBlank()) {
                Column {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        items(suggestionChips) { suggestionName ->
                            SuggestionChip(
                                onClick = {
                                    val uniqueList = blockedList.mapIndexed { index, item ->
                                        if (index == 0) item.copy(text = suggestionName) else item
                                    }
                                    blockedList.clear()
                                    blockedList.addAll(uniqueList)

                                    onEvent(EventActions.ChangeBlockedWebs(blockedList.map { it.text }))
                                },
                                label = { Text(suggestionName) }
                            )
                        }
                    }
                }
            }
        }

        item {
            if (blockedList.any { it.text.isNotBlank() }) {
                Column {
                    HorizontalDivider()
                    AddItemButton("Allowed websites") {
                        if (allowedList.getOrNull(0)?.text?.isBlank() != true) {
                            allowedList.add(0, UniqueString(""))
                        }
                    }
                }
            }
        }

        if (blockedList.any { it.text.isNotBlank() }) {
            itemsIndexed(
                items = allowedList,
                key = { _, item -> item.id }
            ) { index, item ->
                WebsiteItem(
                    itemName = item.text,
                    onValueChange = { newText ->
                        allowedList[index] = allowedList[index].copy(text = newText)
                    },
                    hint = "Type allowed URL...",
                    checkDuplicatedItem = {
                        val uniqueBlockedList = filterList(blockedList, allowedList)
                        blockedList.clear()
                        blockedList.addAll(uniqueBlockedList)

                        val uniqueAllowedList = filterList(allowedList, blockedList)
                        allowedList.clear()
                        allowedList.addAll(uniqueAllowedList)

                        onEvent(EventActions.ChangeBlockedWebs(blockedList.map { it.text }))
                        onEvent(EventActions.ChangeAllowedWebs(allowedList.map { it.text }))
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
        if (itemName.isBlank() && !isFocused) {
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
                        onValueChange("")
                        checkDuplicatedItem()
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
                        .padding(16.dp)
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
    val ytList = remember { mutableStateListOf<UniqueString>() }
    ytList.addAll(state.customAppsYt.map { UniqueString(text = it) })
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
                        checked = ytList.any { it.text == "BLOCK_SHORTS" },
                        onCheckedChange = { updateCustomAppsList("BLOCK_SHORTS", ytList, onEvent) }
                    )
                },
                modifier = Modifier.clickable { updateCustomAppsList("BLOCK_SHORTS", ytList, onEvent) }.padding(horizontal = 12.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Block all videos") },
                trailingContent = {
                    Switch(
                        checked = ytList.any { it.text == "BLOCK_ALL" },
                        onCheckedChange = { updateCustomAppsList("BLOCK_ALL", ytList, onEvent) }
                    )
                },
                modifier = Modifier.clickable { updateCustomAppsList("BLOCK_ALL", ytList, onEvent) }.padding(horizontal = 12.dp)
            )
        }

        item {
            AnimatedVisibility(
                visible = ytList.any { it.text == "BLOCK_ALL" },
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                AddItemButton("Exception keywords") {
                    if (ytList.filter { it.text != "BLOCK_ALL" && it.text != "BLOCK_SHORTS" }.getOrNull(0)?.text?.isBlank() != true) {
                        ytList.add(0, UniqueString(""))
                    }
                }
            }
        }

        if (ytList.any { it.text == "BLOCK_ALL" }) { // only show lists if i enable block videos
            itemsIndexed(
                items = ytList.filter { it.text != "BLOCK_ALL" && it.text != "BLOCK_SHORTS" },
                key = { _, item -> item.id }
            ) { index, item ->
                WebsiteItem(
                    itemName = item.text,
                    onValueChange = { newText ->
                        ytList[index] = ytList[index].copy(text = newText)
                    },
                    hint = "Type video keyword or channel...",
                    checkDuplicatedItem = {
                        val uniqueList = ytList.filter { it.text.isNotBlank() }.distinctBy { it.text }
                        ytList.clear()
                        ytList.addAll(uniqueList)
                        onEvent(EventActions.ChangeCustomApps(ytList.map { it.text }))
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

fun updateCustomAppsList(text: String, list: SnapshotStateList<UniqueString>, onEvent: (EventActions) -> Unit) {
    if (list.find { it.text == text } != null) {
        list.removeIf { it.text == text }
    } else {
        list.add(UniqueString(text = text))
    }
    onEvent(EventActions.ChangeCustomApps(list.map { it.text }))
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