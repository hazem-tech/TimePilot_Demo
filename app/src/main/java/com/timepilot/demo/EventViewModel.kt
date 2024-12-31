package com.timepilot.demo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModel(private val dao: EventDao): ViewModel() {
    private val _state = MutableStateFlow(EventsStates())
    private val _dateSelected = MutableStateFlow(String.toString())
    private val _events = _dateSelected.flatMapLatest { newDate -> dao.getEvents(newDate) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _dateSelected, _events) { state, _, events ->
        state.copy(allEvent = events)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventsStates())

    fun onEvent(input: EventActions) {
        when (input) {
            is EventActions.SetEventName -> {
                _state.update {
                    it.copy(eventName = input.eventName)
                }
            }
            is EventActions.DeleteEvent -> {
                viewModelScope.launch {
                    dao.deleteEvent(input.event)
                }
            }
            is EventActions.ChangeDate -> {
                _state.update {
                    it.copy(date = input.date)
                }
            }

            is EventActions.SetMinTime -> {
                _state.update {
                    it.copy(minTime = input.minTime)
                }
                Log.d("EventActions", "SetMinTime: ${input.minTime}")
            }
            is EventActions.SetMaxTime -> {
                _state.update {
                    it.copy(maxTime = input.maxTime)
                }
                Log.d("EventActions", "SetMaxTime: ${input.maxTime}")
            }
            is EventActions.SetTrackingMode -> {
                _state.update {
                    it.copy(trackingMode = input.trackingMode)
                }
                Log.d("EventActions", "SetTrackingMode: ${input.trackingMode}")
            }
            is EventActions.ChangeAnytime -> {
                _state.update {
                    it.copy(anyTimeTask = input.anyTimeTask)
                }
                Log.d("EventActions", "ChangeAnytime: ${input.anyTimeTask}")
            }
            is EventActions.SetRepeat -> {
                _state.update {
                    it.copy(repeat = input.repeat)
                }
                Log.d("EventActions", "SetRepeat: ${input.repeat}")
            }

            is EventActions.SetColor -> {
                _state.update {
                    it.copy(eventColor = input.eventColor)
                }
            }
            is EventActions.ChangeAllowedApps -> {
                _state.update {
                    it.copy(allowedApps = input.allowedApps)
                }
            }
            is EventActions.ChangeBlockedWebs -> {
                _state.update {
                    it.copy(blockedWebs = input.blockedWebsites)
                }
            }
            is EventActions.ChangeAllowedWebs -> {
                _state.update {
                    it.copy(allowedWebs = input.allowedWebsites)
                }
            }
            is EventActions.ChangeCustomApps -> {
                _state.update {
                    it.copy(customAppsYt = input.youtube)
                }
            }

            is EventActions.HideSheet -> {
                if (input.saveEvent) {
                    val newEvent = Event(
                        eventName = state.value.eventName,
                        date = state.value.date,
                        minTime = state.value.minTime,
                        maxTime = state.value.maxTime,
                        trackingMode = state.value.trackingMode,
                        anyTimeEvent = state.value.anyTimeTask,
                        eventColor = state.value.eventColor,
                        repeat = state.value.repeat,
                        position = state.value.position,
                        allowedApps = state.value.allowedApps,
                        blockedWebs = state.value.blockedWebs,
                        allowedWebs = state.value.allowedWebs,
                        customAppsYt = state.value.customAppsYt
                    )
                    // only upsert if the new event is not empty and not the same as a new event, diff id and date is not enough to save
                    if (newEvent.copy(id = 0, date = "", position = 0) != Event(id = 0, date = "", position = 0)) {
                        if (state.value.alreadyCreatedEvent != null) {
                            newEvent.id = state.value.alreadyCreatedEvent!!
                        }
                        viewModelScope.launch {
                            dao.upsertEvent(newEvent)
                        }
                    }
                }
                _state.update {
                    it.copy(
                        isPartialSheet = false,
                        isForcedSheet = false,
                        isFullSheet = false,
                        alreadyCreatedEvent = null,
                        sheetHideMoreOptions = true
                    )
                }
            }
            is EventActions.ShowFullSheet -> {
                _state.update {
                    it.copy(isPartialSheet = false, isForcedSheet = false, isFullSheet = true)
                }
                if (input.eventID != null) {
                    _state.update {
                        it.copy(alreadyCreatedEvent = input.eventID)
                    }
                }
            }
            EventActions.ShowForceFullSheet -> {
                _state.update {
                    it.copy(isPartialSheet = false, isForcedSheet = true, isFullSheet = false)
                }
            }
            EventActions.ShowPartialSheet -> {
                _state.update {
                    it.copy(isPartialSheet = true, isForcedSheet = false, isFullSheet = false, sheetHideMoreOptions = true)
                }
            }

            is EventActions.ChangeDay -> {
                _dateSelected.value = input.newDay
                Log.d("EventActions", "DayChanged: ${input.newDay}")
            }

            is EventActions.SetUpStates -> {
                _state.value = _state.value.copy(
                    eventName = input.event.eventName,
                    date = input.event.date,
                    anyTimeTask = input.event.anyTimeEvent,
                    minTime = input.event.minTime,
                    maxTime = input.event.maxTime,
                    trackingMode = input.event.trackingMode,
                    eventColor = input.event.eventColor,
                    repeat = input.event.repeat,
                    position = input.event.position,
                    allowedApps = input.event.allowedApps,
                    blockedWebs = input.event.blockedWebs,
                    allowedWebs = input.event.allowedWebs,
                    customAppsYt = input.event.customAppsYt,
                )
            }

            is EventActions.ChangeEventPosition -> {
                viewModelScope.launch {
                    dao.updateEvent(input.event)
                }
            }

            is EventActions.SwitchHideMoreOptions -> {
                _state.update {
                    it.copy(sheetHideMoreOptions = input.value)
                }
            }

            is EventActions.UpdateInstalledApps -> {
                _state.update {
                    it.copy(allInstalledApps = input.allUserApps)
                }
            }
        }
    }
}