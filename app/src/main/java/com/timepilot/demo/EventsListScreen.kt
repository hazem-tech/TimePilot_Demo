package com.timepilot.demo

import android.graphics.BlurMaskFilter
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDate

@Composable
fun EventsList(
    state: EventsStates,
    onEvent: (EventActions) -> Unit,
    allApps: List<App>,
    colors: List<Pair<String, Color>>,
    navController: NavController
) {
    val primaryColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryColor = MaterialTheme.colorScheme.secondaryContainer
    val view = LocalView.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onEvent(EventActions.ChangeEventPosition(state.allEvent[from.index].copy(position = to.index)))
        onEvent(EventActions.ChangeEventPosition(state.allEvent[to.index].copy(position = from.index)))

        ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK)
    }
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var lastSelected = LocalDate.now()

    Scaffold(contentColor = MaterialTheme.colorScheme.onSurface) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            // TODO() the lazy column for the events and the calendar cells works with slide animation

            Column {
                CalendarBar(selected, { selected = it }, onEvent, navController)
                AnimatedVisibility(state.allEvent.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        contentPadding = PaddingValues(
                            start = 9.dp,
                            end = 9.dp,
                            top = 12.dp,
                            bottom = 40.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                    ) {
                        items(state.allEvent.sortedBy { it.position }, key = { it.id }) { event ->
                            ReorderableItem(reorderableLazyListState, key = event.id) {
                                EventCard(
                                    event = event,
                                    allowedApps = state.allowedApps,
                                    modifier = Modifier
                                        .clickable {
                                            onEvent(EventActions.SetUpStates(event))
                                            onEvent(EventActions.ShowFullSheet(event.id))
                                        }
                                        .longPressDraggableHandle(
                                            onDragStarted = {
                                                ViewCompat.performHapticFeedback(
                                                    view,
                                                    HapticFeedbackConstantsCompat.GESTURE_START
                                                )
                                            },
                                            onDragStopped = {
                                                ViewCompat.performHapticFeedback(
                                                    view,
                                                    HapticFeedbackConstantsCompat.GESTURE_END
                                                )
                                            },
                                        ),
                                    startOnClick = {},
                                    pauseOnClick = {},
                                    markOnClick = {},
                                    backgroundBarColor = colors.find { it.first == event.eventColor }?.second ?: Color.Red,
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.allEvent.isEmpty(),
                enter = slideInHorizontally(
                    initialOffsetX = { if (lastSelected > selected) -it else it },
                    animationSpec = tween(300, easing = customEasing)),
                exit = slideOutHorizontally(
                    targetOffsetX = { if (lastSelected > selected) it else -it },
                    animationSpec = tween(300, easing = customEasing))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.empty_list_vector),
                        contentDescription = null,
                    )
                    Text(
                        text = "Start planning your day!",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(30.dp)
                    )
                }
                lastSelected = selected
            }

            Button(
                contentPadding = PaddingValues(horizontal = 30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .border(
                        1.dp, Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        ),
                        CircleShape
                    )
                    .drawBehind {
                        drawIntoCanvas { canvas ->
                            val topShadowPaint = Paint().apply {
                                color = secondaryColor.copy(alpha = 0.7f)
                                asFrameworkPaint().maskFilter =
                                    BlurMaskFilter(90f, BlurMaskFilter.Blur.NORMAL)
                            }
                            val bottomShadowPaint = Paint().apply {
                                color = primaryColor.copy(alpha = 0.7f)
                                asFrameworkPaint().maskFilter =
                                    BlurMaskFilter(110f, BlurMaskFilter.Blur.NORMAL)
                            }
                            // top change the size, bottom change how it will be cut from the top
                            canvas.drawRect(0f, -15f, size.width, 60f, topShadowPaint)
                            canvas.drawRect(
                                0f,
                                size.height + 30f,
                                size.width,
                                90f,
                                bottomShadowPaint
                            )
                        }
                    },
                onClick = {
                    onEvent(EventActions.SetUpStates(
                        Event(date = selected.toString(),
                            position = state.allEvent.map { it.date == selected.toString() }.size // size of current date position
                        )
                    ))
                    onEvent(EventActions.ShowPartialSheet)
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    Modifier.size(30.dp)
                )
                Text(
                    text = "Add new",
                    fontSize = 19.sp,
                    modifier = Modifier.padding(vertical = 20.dp, horizontal = 10.dp)
                )
            }
        }

        CustomBottomSheet(state, onEvent) { sheetHeight ->
            val sheetNavController = rememberNavController()
            NavHost(
                navController = sheetNavController,
                startDestination = "eventSheet",
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start, tween(
                            350,
                            easing = customEasing
                        ),
                        initialOffset = { 300 }) + fadeIn(tween(150))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start, tween(
                            350,
                            easing = customEasing
                        ),
                        targetOffset = { -300 }) + fadeOut(tween(150))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End, tween(
                            350,
                            easing = customEasing
                        ),
                        initialOffset = { -300 }) + fadeIn(tween(150))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End, tween(
                            350,
                            easing = customEasing
                        ),
                        targetOffset = { 300 }) + fadeOut(tween(150))
                }
            ) {
                composable("eventSheet") {
                    NewEvent(state = state, onEvent = onEvent, sheetHeight = sheetHeight, colors = colors, navController = sheetNavController)
                }

                composable("appsWebsScreen") {
                    AppsWebsScreen(state, onEvent, allApps, sheetNavController)
                }

                navigation(
                    startDestination = "eventDurationScreen",
                    route = "eventDuration"
                ) {
                    composable("eventDurationScreen") {
                        EventDuration(state, onEvent, sheetNavController)
                    }
                    composable("eventTrackingScreen") {
                        EventTracking(state, onEvent, sheetNavController)
                    }
                }

                composable("eventRepeat") {
                    RepeatScreen(state, onEvent, sheetNavController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimePilotDemoTheme {
        // EventsList(colors= ColorOption(
        //                        name = "Main",
        //                        backgroundColor = if (!isSystemInDarkTheme()) colorResource(com.google.android.material.R.color.material_dynamic_secondary90)
        //                        else colorResource(com.google.android.material.R.color.material_dynamic_secondary10),
        //                        backgroundBarColor = if (!isSystemInDarkTheme()) colorResource(com.google.android.material.R.color.material_dynamic_primary80)
        //                        else colorResource(com.google.android.material.R.color.material_dynamic_primary10),
        //                        buttonColor = MaterialTheme.colorScheme.primary.copy(0.3f)
        //                    ), rememberNavController())
    }
}