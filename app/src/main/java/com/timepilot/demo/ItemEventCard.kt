package com.timepilot.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@Composable
fun EventCard(
    event: Event,
    allowedApps: List<String>,
    startOnClick: () -> Unit,
    pauseOnClick: () -> Unit,
    markOnClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<ColorOption>,
    previewHideIcons: Boolean = false
) {
    val backgroundColor = colors.find { it.name == event.eventColor }!!.backgroundColor
    val backgroundBarColor = colors.find { it.name == event.eventColor }!!.backgroundBarColor
    val buttonColor = colors.find { it.name == event.eventColor }!!.buttonColor
    val taskCompletePercentage = 100 // todo

    // or Surface, or Card, i have no idea
    Box(modifier.clip(RoundedCornerShape(18.dp))) {
        Column (
            Modifier.background(
                Brush.linearGradient(
                    colors = listOf(backgroundBarColor, backgroundColor),
                    start = Offset(taskCompletePercentage * 0.8f, 0f),
                    end = Offset(
                        taskCompletePercentage * 1.4f,
                        0f
                    ) // to get around 100 more to make it soft
                )
            ).padding(20.dp)
        ) {
            Row(Modifier.padding(bottom = 20.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = event.eventName.ifBlank { "Untitled" },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (event.repeats.split(",")[0].toInt() > 0) {
                        Text(
                            text = "Repeated every ${event.repeats.split(",")[0].toInt()} ${if (event.repeats.split(",")[1].toInt() == 0) "days" else "weeks"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                            contentDescription = null
                        )
                        Text(
                            text = "${durationFormatting(event.minTime, true)} - ${durationFormatting(event.maxTime, true)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!previewHideIcons) {
                    AppsIcons(
                        allowedApps = allowedApps,
                        horizontalArrangement = Arrangement.spacedBy((-12).dp),
                        iconsSize = 30.dp,
                        backgroundColor = backgroundColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally)
            ) {
                FilledTonalButton(
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = buttonColor,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    onClick = if (event.eventStatus == 0) startOnClick else pauseOnClick
                ) {
                    Icon(
                        imageVector = if (event.eventStatus == 0) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Text(
                        text = if (event.eventStatus == 0) "Start" else "Pause",
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp)
                    )
                }

                if (event.eventStatus != 0) { // show only after we start, even if it's gonna disabled
                    FilledTonalButton(
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = buttonColor,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        onClick = markOnClick
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null
                        )
                        Text(
                            text = "Mark as done",
                            Modifier.padding(start = 3.dp, top = 12.dp, bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppsIcons(
    allowedApps: List<String>,
    horizontalArrangement: Arrangement.Horizontal,
    iconsSize: Dp,
    backgroundColor: Color? = null,
    onClick: () -> Unit = {},
    minApps: Int = 1,
    maxApps: Int = 4,
    content: @Composable () -> Unit = {}
) {
    val pm = LocalContext.current.packageManager
    Row(
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier.height(iconsSize + 5.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        content()

        if (allowedApps.isNotEmpty()) {
            for (packageName in allowedApps.take(maxApps)) {
                Box(contentAlignment = Alignment.Center) {
                    // border, to get the same of a sample icon
                    if (backgroundColor != null) {
                        Image(
                            bitmap = pm.getApplicationIcon("com.android.settings")
                                .toBitmap().asImageBitmap(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                backgroundColor,
                                BlendMode.SrcAtop
                            )
                        )
                    }

                    Image(
                        bitmap = pm.getApplicationIcon(packageName).toBitmap()
                            .asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(iconsSize)
                    )
                }
            }
        }

        if (allowedApps.size !in minApps..maxApps) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    bitmap = pm.getApplicationIcon("com.android.settings")
                        .toBitmap()
                        .asImageBitmap(),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        BlendMode.SrcAtop
                    )
                )
                Text(
                    text = if (allowedApps.size > maxApps) "%d+".format(allowedApps.size - maxApps) else "0",
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Preview
@Composable
fun CardPreview() {
    TimePilotDemoTheme {
        EventCard(
            event = Event(date = "12-12-2025", position = 0),
            allowedApps = listOf(),
            colors = listOf(ColorOption(name = "Main", backgroundColor = MaterialTheme.colorScheme.primaryContainer, backgroundBarColor = MaterialTheme.colorScheme.secondaryContainer, buttonColor = MaterialTheme.colorScheme.primary.copy(0.3f))),
            startOnClick = {}, pauseOnClick = {}, markOnClick = {}, previewHideIcons = true
        )
    }
}