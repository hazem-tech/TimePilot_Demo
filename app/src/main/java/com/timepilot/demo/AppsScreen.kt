package com.timepilot.demo

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsWebsScreen(allAppsList: List<App>, navController: NavController) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Apps", "Websites", "Custom")

    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        CenterAlignedTopAppBar(
            title = {
                SingleChoiceSegmentedButtonRow(Modifier.width(250.dp)) {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = { selectedIndex = index },
                            selected = index == selectedIndex,
                            label = { Text(label) },
                            icon = {}
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            },
            windowInsets = WindowInsets(0.dp)
        )
        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = { slideInHorizontally(initialOffsetX = {
                if (selectedIndex == 0) -it else it
            }) togetherWith slideOutHorizontally(targetOffsetX = {
                if (selectedIndex == 0) it else -it
            })
            }, label = "AppsWebsCustomTransition"
        ) { targetState ->
            when (targetState) {
                0 -> AppsScreen(allAppsList)
                1 -> WebsScreen()
                2 -> CustomAppScreen()
            }
        }
    }
}

@Composable
fun AppsScreen(allAppsList: List<App>) {
    val searchQuery = remember { mutableStateOf("") }
    val filteredList = remember(searchQuery.value) {
        allAppsList.filter { it.name.contains(searchQuery.value, ignoreCase = true) }
    }
    val selectedApps = remember { mutableStateListOf<App>() }

    val gridState = rememberLazyGridState()
    val rowState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val appsGridCells = 4
    val screenWidth = (LocalConfiguration.current.screenWidthDp.dp - 20.dp)  / appsGridCells

    Column {
        AppsSearchBar(
            textState = searchQuery,
            onValueChange = {
                searchQuery.value = it
                coroutineScope.launch {
                    gridState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp),
            menuSelectAllOnClick = {
                selectedApps.clear()
                selectedApps.addAll(filteredList)
                filteredList.forEach { it.added.value = true }
            }
        )

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(appsGridCells),
            horizontalArrangement = Arrangement.spacedBy((-20).dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 60.dp)
        ) {
            item(span = { GridItemSpan(appsGridCells) }) {
                AnimatedVisibility(selectedApps.isNotEmpty() && searchQuery.value.isEmpty()) {
                    SelectedAppsRow(selectedApps, rowState, screenWidth) { index ->
                        val app = selectedApps[index]
                        selectedApps.remove(selectedApps[index])
                        filteredList[filteredList.indexOf(app)].added.value = false
                    }
                }
                if (selectedApps.isNotEmpty() && searchQuery.value.isEmpty()) {
                    coroutineScope.launch {
                        gridState.animateScrollToItem(0)
                    }
                }
            }

            items(filteredList.size) { index ->
                AppItem(app = filteredList[index], selectedBar = false, onClick = {
                    filteredList[index].added.value = if (!filteredList[index].added.value) {
                        selectedApps.add(0, filteredList[index])
                        true
                    } else {
                        selectedApps.remove(filteredList[index])
                        false
                    }

                    if (filteredList.filter { it.added.value }.size > 4) {
                        coroutineScope.launch {
                            if (rowState.firstVisibleItemIndex == 0) {
                                rowState.scrollToItem(0)
                                Log.d("AnimateScroll", "normal scroll row")
                            } else {
                                rowState.animateScrollToItem(0)
                                Log.d("AnimateScroll", "animated scroll row")
                            }
                        }
                    }
                })
            }
        }
    }
}

@Composable
fun SelectedAppsRow(selectedApps: SnapshotStateList<App>, state: LazyListState, screenWidth: Dp, onClick: (Int) -> Unit) {
    Column {
        SmallText("Allowed apps")
        LazyRow(state = state, contentPadding = PaddingValues(horizontal = 10.dp), modifier = Modifier.fillMaxWidth()) {
            items(
                items = selectedApps,
                key = { it.id }
            ) { item ->
                AppItem(
                    app = item,
                    modifier = Modifier.width(screenWidth).animateItem(
                        fadeInSpec = tween(durationMillis = 300),
                        fadeOutSpec = tween(durationMillis = 300),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )),
                    selectedBar = true,
                    onClick = { onClick(selectedApps.indexOf(item)) }
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            SmallText("All apps")
            HorizontalDivider(Modifier.width(140.dp).align(Alignment.Center))
        }
    }
}

@Composable
fun SmallText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Normal,
        modifier = modifier.padding(start = 16.dp)
    )
}

@Composable
fun AppsSearchBar(
    textState: MutableState<String>,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    menuSelectAllOnClick: () -> Unit = {}
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
                            text = { Text("Select all") },
                            onClick = menuSelectAllOnClick
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by app usage") },
                            onClick = {
                                // todo
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(visible = textState.value.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Empty search text",
                    modifier = Modifier.clickable {
                        textState.value = ""
                        focusManager.clearFocus()
                    }.padding(16.dp)
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

@Composable
fun AppItem(modifier: Modifier = Modifier, app: App, selectedBar: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 24.dp).clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
    ) {
        Box {
            Box(Modifier.size(55.dp)
                .shadow(elevation = 10.dp, shape = CircleShape,
                spotColor = Color.Black.copy(alpha = 0.5f))
            )
            Image(
                painter = app.icon,
                contentDescription = null,
                modifier = Modifier.size(58.dp)
            )
            CircularCheckbox(
                checked = app.added.value,
                modifier = Modifier.offset(x = (-4).dp, y = (-2).dp),
                selectedBar = selectedBar
            )
        }
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp, start = 1.dp, end = 1.dp)
        )
    }
}

@Composable
fun CircularCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    selectedBar: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.34f),
        label = "checkBoxCheckAnimation"
    )

    Box(
        modifier = modifier
            .size(19.dp)
            .background(
                color = if (!selectedBar) backgroundColor else Color(0xFFF9DEDC),
                shape = CircleShape
            )
            .border(width = 1.dp, color = Color(0x40FFFFFF), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!selectedBar) {
            AnimatedVisibility(checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = null,
                tint = Color(0xFFB3261E),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview
@Composable
fun AppsPreview() {
    TimePilotDemoTheme {
        AppsWebsScreen(
            listOf(
                List(80) {
                    App(name = "$it Hello", packageName = it.toString(), icon = painterResource(id = R.drawable.ic_launcher_background))
                }
            ).flatten(),
            rememberNavController()
        )
    }
}