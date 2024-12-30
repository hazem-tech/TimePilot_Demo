package com.timepilot.demo

import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
    startOnClick: () -> Unit,
    pauseOnClick: () -> Unit,
    markOnClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundBarColor: Color,
    previewHideIcons: Boolean = false
) {
    val percentage = 0.4f
    val color = if (!isSystemInDarkTheme()) 1f else 0f
    val r = backgroundBarColor.red * (1 - percentage) + color * percentage
    val g = backgroundBarColor.green * (1 - percentage) + color * percentage
    val b = backgroundBarColor.blue * (1 - percentage) + color * percentage
    val backgroundColor = Color(r, g, b)
    val buttonColor = if (!isSystemInDarkTheme()) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)

    // or Surface, or Card, i have no idea
    Box(modifier.clip(RoundedCornerShape(18.dp))) {
        Column (
            Modifier.background(
                Brush.linearGradient(
                    colors = listOf(backgroundBarColor, backgroundColor),
                    start = Offset(event.timeSpent * 0.8f, 0f),
                    end = Offset(event.timeSpent * 1.4f, 0f) // to get around 100 more to make it soft
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
                    if (event.repeat[0].toInt() > 0) {
                        Text(
                            text = "Repeated every ${event.repeat[0].toInt()} ${if (event.repeat[1] == "DAILY") "days" else "weeks"}",
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
                        allowedApps = event.allowedApps,
                        horizontalArrangement = Arrangement.spacedBy((-12).dp),
                        iconsSize = 30.dp,
                        backgroundColor = backgroundColor,
                        showEmpty = true
                    )
                }
            }

            // TODO() show start button if no even is selected and running && (this is the first task in the day or anytime task is enabled)
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
                    onClick = if (event.eventStatus == EventStatus.NEVER_STARTED) startOnClick else pauseOnClick
                ) {
                    Icon(
                        imageVector = if (event.eventStatus == EventStatus.NEVER_STARTED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Text(
                        text = if (event.eventStatus == EventStatus.NEVER_STARTED) "Start" else "Pause",
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 12.dp)
                    )
                }

                if (event.eventStatus != EventStatus.NEVER_STARTED) { // show only after we start, even if it's gonna disabled
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
    showEmpty: Boolean,
    maxApps: Int = 4,
    content: @Composable () -> Unit = {}
) {
    val pm = LocalContext.current.packageManager
    Row(
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier.height(iconsSize + 6.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        content()
        var loop = allowedApps.take(maxApps).size
        if ((showEmpty && allowedApps.isEmpty()) || allowedApps.size > maxApps) {
            loop += 1
        }
        for (i in 0 until loop) {
            Box(contentAlignment = Alignment.Center) {
                // border, to get the same of a sample icon
                if (backgroundColor != null) {
                    Image(
                        bitmap = pm.getApplicationIcon("com.android.settings").toBitmap().asImageBitmap(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(backgroundColor, BlendMode.SrcAtop)
                    )
                }

                if ((showEmpty && allowedApps.isEmpty()) || i >= maxApps) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(iconsSize)) {
                        Image(
                            bitmap = pm.getApplicationIcon("com.android.settings").toBitmap().asImageBitmap(),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceContainerLow, BlendMode.SrcAtop)
                        )
                        Text(
                            text = if (allowedApps.isNotEmpty()) "%d+".format(allowedApps.size - maxApps) else "0",
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Log.d("AppsIcons", "AppsIcons: $allowedApps, size ${allowedApps.size}")
                    val iconBitmap = try {
                        pm.getApplicationIcon(allowedApps[i]).toBitmap().asImageBitmap()
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }

                    if (iconBitmap != null) {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = null,
                            modifier = Modifier.size(iconsSize)
                        )
                    }
                }
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
            backgroundBarColor = MaterialTheme.colorScheme.primaryContainer,
            startOnClick = {}, pauseOnClick = {}, markOnClick = {}, previewHideIcons = true
        )
    }
}