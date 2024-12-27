package com.timepilot.demo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.UUID

data class App(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val icon: Painter,
    var added: MutableState<Boolean> = mutableStateOf(false)
)

@Entity
data class Event(
    var eventName: String = "",
    var date: Long, // need to set current date in milliseconds
    var minTime: Int = 15,
    var maxTime: Int = 30,
    var trackingMode: EventTrackingMethod = EventTrackingMethod.COUNTDOWN,
    var timeSpent: Int = 0, // in seconds
    var allowedApps: List<String> = listOf(), // package names
    var blockedWebs: List<String> = listOf(),
    var allowedWebs: List<String> = listOf(),
    var customYoutube: List<String> = listOf(),
    var eventColor: EventCardColor = EventCardColor.MAIN,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)

enum class EventTrackingMethod {
    COUNTDOWN,
    APP_USAGE
}

enum class EventCardColor {
    MAIN,
    BLUE,
    RED,
    GREEN,
    YELLOW,
    PURPLE,
    PINK
}

class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }
}