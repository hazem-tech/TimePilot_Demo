package com.timepilot.demo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.timepilot.demo.ui.theme.TimePilotDemoTheme
import java.time.LocalDate
import kotlin.math.absoluteValue
import kotlin.math.pow

@Composable
fun CalendarBar(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { 260 }, initialPage = 130) // 5 yrs
    var selected by remember { mutableStateOf(LocalDate.now()) }
    var month by remember { mutableStateOf(selected) }
    var currentCellWeek by remember { mutableIntStateOf(0) }
    var previousPage by remember { mutableIntStateOf(0) }
    val accountCreated by remember { mutableStateOf(false) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 25.dp)) {
            AnimatedContent(
                targetState = month,
                transitionSpec = {
                    val isForward = targetState > initialState
                    slideInHorizontally(initialOffsetX = { fullWidth -> if (isForward) fullWidth else -fullWidth }) +
                            fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                            slideOutHorizontally(targetOffsetX = { fullWidth -> if (isForward) -fullWidth else fullWidth }) +
                            fadeOut(animationSpec = tween(durationMillis = 100))
                },
                label = "MonthSwipeAnimation",
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 25.dp)
            ) { targetValue ->
                // TODO() should add a button to choose the month, but the app is so new no one will need this and see "history"
                Text(
                    text = targetValue.month.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 25.dp)
                )
            }

            FilledIconButton(
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = contentColorFor(MaterialTheme.colorScheme.tertiary)
                ),
                modifier = Modifier.size(45.dp),
                onClick = {
                    navController.navigate("settings")  {
                        launchSingleTop = true
                    }
                }
            ) {
                if (!accountCreated) {
                    Icon(
                        imageVector = Icons.Filled.Face,
                        contentDescription = "Settings",
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Settings",
                    )
                }
            }
        }

        Row(modifier = Modifier.padding(horizontal = 9.dp)) {
            for (i in 0..6)
                Text(
                    text = LocalDate.now().plusDays(i.toLong()).dayOfWeek.name.take(1),
                    fontSize = 11.sp,
                    lineHeight = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
        }

        HorizontalPager(state = pagerState) { page ->
            val week = if (page in 130..259)
                (0..6).map { LocalDate.now().plusDays((page - 130) * 7 + it.toLong()) }
            else if (page > 0)
                (1..7).map {
                    LocalDate.now().minusDays((page - 129).absoluteValue * 7 + it.toLong())
                }.reversed()
            else
                (0..6).map { LocalDate.now().plusDays(it.toLong()) }

            if (pagerState.currentPage != previousPage && pagerState.currentPage == page) {
                previousPage = page
                selected = week[currentCellWeek]
                if (month.month != selected.month) month = selected
                // TODO() change list date
            }

            Row(modifier = Modifier.padding(horizontal = 9.dp)) {
                for (i in 0..6)
                    DayView(
                        modifier = Modifier.weight(1f),
                        date = week[i],
                        selected = selected,
                        updateSelected = {
                            currentCellWeek = i
                            previousPage = -1
                        }
                    )
            }
        }
        HorizontalDivider()
    }
}

@Composable
fun DayView(modifier: Modifier = Modifier, date: LocalDate, selected: LocalDate, updateSelected: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (selected == date) 1f else 0.6f,
        animationSpec = TweenSpec(durationMillis = 300, easing = customEasing),
        label = "CircleDayViewAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = updateSelected
            )
    ) {
        // Two boxes to make the animation works
        Box(
            modifier = Modifier
                .padding(vertical = 9.dp, horizontal = 6.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            Box(
                modifier =
                if (selected == date)
                    Modifier
                        .clip(CircleShape)
                        .aspectRatio(1f)
                        .background(
                            if (selected == LocalDate.now())
                                if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                else
                    Modifier.aspectRatio(1f)
            )
        }
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 22.sp,
            fontWeight = if (selected == date) FontWeight.Bold else FontWeight.Normal,
            color = if (selected == date && selected == LocalDate.now())
                Color.White
            else if(selected == date)
                MaterialTheme.colorScheme.surface
            else if (date == LocalDate.now())
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    TimePilotDemoTheme {
        CalendarBar(rememberNavController())
    }
}