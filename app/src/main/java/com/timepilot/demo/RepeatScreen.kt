package com.timepilot.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatScreen(eventName: String, navController: NavController) {
    val numState = rememberPagerState(pageCount = { 48 }, initialPage = 0)
    val typeState = rememberPagerState(pageCount = { 2 })
    val numList = (0..48).toList()
    val weeksList = remember {
        mutableStateListOf(
            Pair("Sunday", false),
            Pair("Monday", false),
            Pair("Tuesday", false),
            Pair("Wednesday", false),
            Pair("Thursday", false),
            Pair("Friday", false),
            Pair("Saturday", false)
        )
    }

    Column(Modifier.background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        LargeTopAppBar(
            title = { Text(
                if (numState.currentPage == 0)
                    "$eventName will not repeat"
                else
                    "$eventName will repeat every"
            ) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            },
            windowInsets = WindowInsets(0.dp),
        )

        Box(Modifier.padding(bottom = 20.dp)) {
            Row(Modifier.height(280.dp).padding(horizontal = 90.dp)) {
                NumPicker(state = numState,
                    text = { index -> numList[index].toString() },
                    horizontal = false,
                    modifier = Modifier.weight(1f),
                    padding = PaddingValues(vertical = 110.dp),
                    textHeight = 90.dp
                )
                NumPicker(
                    state = typeState,
                    text = { index ->
                        val type = if (numState.currentPage < 2) listOf("day", "week") else listOf("days", "weeks")
                        type[index]
                    },
                    horizontal = false,
                    modifier = Modifier.weight(1f),
                    padding = PaddingValues(vertical = 100.dp),
                    textHeight = 90.dp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(60.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .border(
                        2.dp, Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                            )
                        ),
                        CircleShape
                    )
            )
        }

        AnimatedVisibility(visible = typeState.currentPage == 1 && numState.currentPage > 0) {
            Column(Modifier.padding(top = 5.dp, bottom = 40.dp)) {
                HorizontalDivider()

                weeksList.forEachIndexed { index, (name, selected) ->
                    ListItem(
                        headlineContent = { Text(name) },
                        trailingContent = {
                            AnimatedVisibility(
                                visible = selected,
                                enter = scaleIn(initialScale = 0f, animationSpec = tween(200, easing = customEasing)),
                                exit = scaleOut(targetScale = 0f, animationSpec = tween(200, easing = customEasing))
                            ) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            weeksList[index] = weeksList[index].copy(second = !selected)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NumPicker(modifier: Modifier = Modifier, state: PagerState, text: (Int) -> String, horizontal: Boolean, padding: PaddingValues, textHeight: Dp) {
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

@Preview
@Composable
fun RepeatPreview() {
    TimePilotDemoTheme {
        RepeatScreen("Hello", rememberNavController())
    }
}