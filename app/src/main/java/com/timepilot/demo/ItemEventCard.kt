package com.timepilot.demo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.timepilot.demo.ui.theme.TimePilotDemoTheme

@Composable
fun EventCard(modifier: Modifier = Modifier, startOnClick: () -> Unit, pauseOnClick: () -> Unit, markOnClick: () -> Unit, showIcons: Boolean = true) {
    val backgroundColor = if (isSystemInDarkTheme()) Color(0xFF15171D) else Color(0xFFDAE9FF)
    val backgroundBarColor = if (isSystemInDarkTheme()) Color(0xFF0D1828) else Color(0xFFB5CDFF)
    val buttonColor = if (isSystemInDarkTheme()) Color(0xFF20263A) else Color(0xFFC2DBF7)
    val taskCompletePercentage = 100
    val allowedAppsList = listOf("com.android.settings", "com.android.settings", "com.android.settings", "com.android.settings", "com.android.settings", "com.android.settings")
    val allowedSize = allowedAppsList.size
    val pm = LocalContext.current.packageManager
    val eventState by remember { mutableStateOf("Start") }

    Box( // or Surface, or Card, i have no idea
        modifier = modifier.clip(RoundedCornerShape(18.dp))
        // todo open up the sheet on onclick
    ) {
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
                        text = "Studying",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    //if (isRepeatable) {
                    Text(
                        text = "Repeated every day",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    //}
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                            contentDescription = null
                        )
                        Text(
                            text = "20m - 30m",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (showIcons) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-12).dp),
                        modifier = Modifier.height(35.dp)
                    ) {
                        if (allowedAppsList.isNotEmpty()) {
                            for (packageName in allowedAppsList.take(4)) {
                                Box(contentAlignment = Alignment.Center) {
                                    // border, to get the same of a sample icon
                                    Image(
                                        bitmap = pm.getApplicationIcon("com.android.settings")
                                            .toBitmap().asImageBitmap(),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(
                                            backgroundColor,
                                            BlendMode.SrcAtop
                                        )
                                    )

                                    Image(
                                        bitmap = pm.getApplicationIcon(packageName).toBitmap()
                                            .asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }

                        if (allowedSize !in 1..4) {
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
                                    text = if (allowedSize > 4) "%d+".format(allowedSize - 4) else "0",
                                    fontSize = 13.sp
                                ) // how many more apps other than the 4 shown
                            }
                        }
                    }
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
                    onClick = if (eventState == "Start") startOnClick else pauseOnClick
                ) {
                    Icon(
                        imageVector = if (eventState == "Start") Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null
                    )
                    Text(eventState, Modifier.padding(horizontal = 3.dp, vertical = 12.dp))
                }

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
                    Text(text = "Mark as done", Modifier.padding(start = 3.dp, top = 12.dp, bottom = 12.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun CardPreview() {
    TimePilotDemoTheme {
        EventCard(showIcons = false, startOnClick = {}, pauseOnClick = {}, markOnClick = {})
    }
}