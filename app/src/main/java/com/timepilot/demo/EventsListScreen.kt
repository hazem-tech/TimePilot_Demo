package com.timepilot.demo

import android.graphics.BlurMaskFilter
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun EventsList(allApps: List<App>, navController: NavController) {
    var sheetMode by remember { mutableIntStateOf(hidden) }
    val primaryColor = MaterialTheme.colorScheme.primaryContainer
    val secondaryColor = MaterialTheme.colorScheme.secondaryContainer

    val view = LocalView.current
    var list by remember { mutableStateOf(List(12) { "Item $it" }) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        list = list.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
    }

    var empty by remember { mutableStateOf(false) }

    Scaffold(contentColor = MaterialTheme.colorScheme.onSurface) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            // TODO() the lazy column for the events and the calendar cells works with slide animation

            Column {
                CalendarBar(navController)
                AnimatedVisibility(!empty) {
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
                        items(list, key = { it }) {
                            ReorderableItem(reorderableLazyListState, key = it) { // isDragging ->
                                EventCard(
                                    modifier = Modifier.longPressDraggableHandle(
                                        onDragStarted = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                                                view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                                        },
                                        onDragStopped = {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                                                view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                                        },
                                    ),
                                    startOnClick = { empty = true },
                                    pauseOnClick = {},
                                    markOnClick = {}
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(empty) {
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
                        2.dp, Brush.horizontalGradient(
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
                onClick = { sheetMode = 1 }
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

        CustomBottomSheet(sheetMode, { sheetMode = it }) { sheetHeight ->
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
                    NewEvent(mode = sheetMode, setSheet = { sheetMode = it }, sheetHeight = sheetHeight, newOne = true, navController = sheetNavController)
                }

                composable("appsWebsScreen") {
                    AppsWebsScreen(allApps, sheetNavController)
                }

                navigation(
                    startDestination = "eventDurationScreen",
                    route = "eventDuration"
                ) {
                    composable("eventDurationScreen") {
                        EventDuration("studying", sheetNavController)
                    }
                    composable("eventTrackingScreen") {
                        EventTracking(sheetNavController)
                    }
                }

                composable("eventRepeat") {
                    RepeatScreen("studying", sheetNavController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TimePilotDemoTheme {
        EventsList(listOf(), rememberNavController())
    }
}