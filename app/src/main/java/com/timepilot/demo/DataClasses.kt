package com.timepilot.demo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity
data class Event(
    var eventName: String = "",
    var date: String,
    var minTime: Int = 15,
    var maxTime: Int = 30,
    var trackingMode: String = "Countdown",
    var timeSpent: Int = 0, // in seconds
    var anyTimeEvent: Boolean = false,
    var eventColor: String = "Main",
    var repeats: String = "0,0", // 0 times, 0 = daily, 1 = weekly as a comma-separated string
    var eventStatus: Int = 0, // 0 = never started, 1 = in progress, 2 = finished
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Event::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AllowedApp(
    @PrimaryKey(autoGenerate = false)
    val packageName: String,
    val eventId: Int
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Event::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class BlockedWebsites(
    @PrimaryKey(autoGenerate = false)
    val webName: String,
    val eventId: Int
)

@Entity(
    foreignKeys = [ForeignKey(
        entity = Event::class,
        parentColumns = ["id"],
        childColumns = ["eventId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AllowedWebsites(
    @PrimaryKey(autoGenerate = false)
    val webName: String,
    val eventId: Int
)

data class EventsStates(
    val allEvent: List<Event> = emptyList(),
    val eventName: String = "",
    val date: String = LocalDate.now().toString(),
    val anyTimeTask: Boolean = false,
    val minTime: Int = 15,
    val maxTime: Int = 30,
    var trackingMode: String = "Countdown",
    var eventColor: String = "Main",
    var repeats: String = "0,0", // 0 times, 0 = daily, 1 = weekly
    var allowedApps: List<String> = listOf(),
    var blockedWebs: List<String> = listOf(),
    var allowedWebs: List<String> = listOf(),
    var customApps: List<String> = listOf(),
    var isPartialSheet: Boolean = false,
    var isFullSheet: Boolean = false,
    var isForcedSheet: Boolean = false,
    var alreadyCreatedEvent: Int? = null
)

sealed interface EventActions {
    data class ChangeDay(val newDay: String): EventActions
    data class HideSheet(val saveEvent: Boolean): EventActions
    data object ShowPartialSheet: EventActions
    data class ShowFullSheet(val eventID: Int?): EventActions
    data object ShowForceFullSheet: EventActions
    data class SetUpStates(val event: Event): EventActions
    data class SetEventName(val eventName: String): EventActions
    data class ChangeDate(val date: String): EventActions
    data class ChangeAnytime(val anyTimeTask: Boolean): EventActions
    data class SetMinTime(val minTime: Int): EventActions
    data class SetMaxTime(val maxTime: Int): EventActions
    data class SetTrackingMode(val trackingMode: String): EventActions
    data class SetColor(val eventColor: String): EventActions
    data class SetRepeat(val frequencyDaysList: String): EventActions
    data class ChangeAllowedApps(val allowedApps: List<String>): EventActions
    data class ChangeBlockedWebs(val blockedWebsites: List<String>): EventActions
    data class ChangeAllowedWebs(val allowedWebsites: List<String>): EventActions
    data class SetCustomApps(val youtube: List<String>): EventActions
    data class DeleteEvent(val event: Event): EventActions
}


data class App(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val packageName: String,
    val icon: Painter,
    var added: MutableState<Boolean> = mutableStateOf(false)
)

data class ColorOption(
    val name: String,
    val backgroundColor: Color,
    val backgroundBarColor: Color,
    val buttonColor: Color
)