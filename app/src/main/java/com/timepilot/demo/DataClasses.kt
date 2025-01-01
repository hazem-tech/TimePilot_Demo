package com.timepilot.demo

import androidx.compose.ui.graphics.painter.Painter
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate
import java.util.UUID

@Entity
data class Event(
    var eventName: String = "Untitled event",
    var date: String, // as yyyy-MM-dd
    var minTime: Int = 15, // in minutes
    var maxTime: Int = 30, // in minutes
    var trackingMode: String = "Countdown",
    var timeSpent: Int = 200, // in seconds
    var eventColor: String = "Main",
    var anyTimeEvent: Boolean = false,
    var eventStatus: EventStatus = EventStatus.NEVER_STARTED,
    var repeat: List<String> = listOf("0", "DAILY"), // repeat[0] times, repeat[1] = type, the rest is the days
    var allowedApps: List<String> = listOf(), // package names
    var blockedWebs: List<String> = listOf(),
    var allowedWebs: List<String> = listOf(),
    var customAppsYt: List<String> = listOf(), // "BLOCK_SHORTS", "BLOCK_ALL", anything else in the list is the exceptions
    var position: Int,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)

data class EventsStates(
    val allEvent: List<Event> = emptyList(),
    val allInstalledApps: List<App> = emptyList(),
    val eventName: String = "Untitled event",
    val date: String = LocalDate.now().toString(),
    val anyTimeTask: Boolean = false,
    val minTime: Int = 15,
    val maxTime: Int = 30,
    var trackingMode: String = "Countdown",
    var eventStatus: EventStatus = EventStatus.NEVER_STARTED,
    var eventColor: String = "Main",
    var repeat: List<String> = listOf("0", "0"),
    var allowedApps: List<String> = listOf(),
    var blockedWebs: List<String> = listOf(),
    var allowedWebs: List<String> = listOf(),
    var customAppsYt: List<String> = listOf(),
    var position: Int = 0,
    var isPartialSheet: Boolean = false,
    var isFullSheet: Boolean = false,
    var isForcedSheet: Boolean = false,
    var sheetHideMoreOptions: Boolean = true,
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
    data class SetRepeat(val repeat: List<String>): EventActions
    data class ChangeAllowedApps(val allowedApps: List<String>): EventActions
    data class ChangeBlockedWebs(val blockedWebsites: List<String>): EventActions
    data class ChangeAllowedWebs(val allowedWebsites: List<String>): EventActions
    data class ChangeCustomApps(val youtube: List<String>): EventActions
    data class ChangeEventPosition(val event: Event): EventActions
    data class SwitchHideMoreOptions(val value: Boolean): EventActions
    data class DeleteEvent(val event: Event): EventActions
    data class UpdateInstalledApps(val allUserApps: List<App>): EventActions
}

data class App(
    val name: String,
    val packageName: String,
    val icon: Painter,
)

data class UniqueString(
    val text: String,
    val id: UUID = UUID.randomUUID()
)

enum class EventStatus {
    NEVER_STARTED,
    IN_PROGRESS,
    FINISHED
}

class Converters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return if (list.isEmpty()) "" else list.joinToString(",")
    }

    @TypeConverter
    fun fromEventStatusType(status: EventStatus): String {
        return status.name
    }

    @TypeConverter
    fun toEventStatusType(value: String): EventStatus {
        return EventStatus.valueOf(value)
    }
}