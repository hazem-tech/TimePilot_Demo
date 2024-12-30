package com.timepilot.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@Composable
fun WebsScreen(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
) {
    val suggestionChips = listOf("Block all websites", "TikTok", "YouTube", "Instagram")

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            WebsiteAddItem {
                val blockedKeywords = state.blockedWebs.toMutableList()
                blockedKeywords.add(0, "")
                onEvent(EventActions.ChangeBlockedWebs(blockedKeywords))
            }
        }

        items(state.blockedWebs) { item ->
            if (item.isEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 24.dp)) {
                    items(suggestionChips) { name ->
                        SuggestionChip(
                            onClick = {
                                // blockedKeywords[0] = name
                                val blockedKeywords = state.blockedWebs.toMutableList()
                                blockedKeywords[0] = name
                                onEvent(EventActions.ChangeBlockedWebs(blockedKeywords))
                            },
                            label = { Text(name) }
                        )
                    }
                }
            }
            WebsiteItem(state, onEvent, item, false)
        }

        item {
            HorizontalDivider(Modifier.padding(top = 12.dp, bottom = 6.dp))
            WebsiteAddItem(true) {
                val allowedKeywords = state.allowedWebs.toMutableList()
                allowedKeywords.add(0, "")
                onEvent(EventActions.ChangeAllowedWebs(allowedKeywords))
            }
        }

        items(state.allowedWebs) { item ->
            WebsiteItem(state, onEvent, item, true)
        }
    }
}

@Composable
fun WebsiteAddItem(allowedItem: Boolean = false, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            TextButton(onClick) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = if (!allowedItem) "Add blocked URL" else "Add allowed URL",
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    )
}

@Composable
fun WebsiteItem(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
    itemName: String,
    allowedItem: Boolean = false,
) {
    ListItem(
        headlineContent = {
            CustomTextField(
                state = state,
                onEvent = onEvent,
                textStyle = MaterialTheme.typography.bodyLarge,
                hint = if (!allowedItem) "Type blocked URL here..." else "Type exception URL here...",
                onDone = {}
            )
        },
        leadingContent = {
            if (itemName.isNotEmpty()) {
                var checked by remember { mutableStateOf(true) }
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }
        }
    )
}

@Composable
fun CustomAppScreen(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
) {
}

@Preview
@Composable
fun WebPreview() {
    TimePilotDemoTheme {
        // WebsScreen()
    }
}