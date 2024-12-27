package com.timepilot.demo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

data class App(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val icon: Painter,
    var added: MutableState<Boolean> = mutableStateOf(false)
)

data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    var eventName: String,
    var status: Int, // = NEW_EVENT_STATUS,
    var timerMode: Int, // = TIMER_EVENT_MODE,
    var minTime: Int = 30, // in minutes
    var maxTime: Int = 60, // in minutes
    var timeSpent: Float = 0f, // in seconds
    var date: Date = Date(),
    var eventDependency: Boolean = false,
    var selectedAppsPackage: MutableList<String> = mutableListOf(), // list cuz it's just packages, mutable list is actual App
    var blockedWebsites: SnapshotStateList<String> = mutableStateListOf(),
    var allowedWebsites: SnapshotStateList<String> = mutableStateListOf(),
    var youtube: SnapshotStateList<String> = mutableStateListOf(),
    var color: List<Color>,
)