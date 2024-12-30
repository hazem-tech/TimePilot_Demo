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

    fun onEvent(event: EventActions) {
        when (event) {
            is EventActions.SetEventName -> {
                _state.update {
                    it.copy(eventName = event.eventName)
                }
            }
            is EventActions.DeleteEvent -> {
                viewModelScope.launch {
                    dao.deleteEvent(event.event)
                }
            }
            is EventActions.ChangeDate -> {
                _state.update {
                    it.copy(date = event.date)
                }
            }

            is EventActions.SetMinTime -> {
                _state.update {
                    it.copy(minTime = event.minTime)
                }
                Log.d("EventActions", "SetMinTime: ${event.minTime}")
            }
            is EventActions.SetMaxTime -> {
                _state.update {
                    it.copy(maxTime = event.maxTime)
                }
                Log.d("EventActions", "SetMaxTime: ${event.maxTime}")
            }
            is EventActions.SetTrackingMode -> {
                _state.update {
                    it.copy(trackingMode = event.trackingMode)
                }
                Log.d("EventActions", "SetTrackingMode: ${event.trackingMode}")
            }
            is EventActions.ChangeAnytime -> {
                _state.update {
                    it.copy(anyTimeTask = event.anyTimeTask)
                }
                Log.d("EventActions", "ChangeAnytime: ${event.anyTimeTask}")
            }
            is EventActions.SetRepeat -> {
                _state.update {
                    it.copy(repeat = event.repeat)
                }
                Log.d("EventActions", "SetRepeat: ${event.repeat}")
            }

            is EventActions.SetColor -> {
                _state.update {
                    it.copy(eventColor = event.eventColor)
                }
            }
            is EventActions.ChangeAllowedApps -> {
                _state.update {
                    it.copy(allowedApps = event.allowedApps)
                }
            }
            is EventActions.ChangeBlockedWebs -> {
                _state.update {
                    it.copy(blockedWebs = event.blockedWebsites)
                }
            }
            is EventActions.ChangeAllowedWebs -> {
                _state.update {
                    it.copy(allowedWebs = event.allowedWebsites)
                }
            }
            is EventActions.SetCustomApps -> {
                _state.update {
                    it.copy(customApps = event.youtube)
                }
            }

            is EventActions.HideSheet -> {
                if (event.saveEvent) {
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
                        customApps = state.value.customApps
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
                if (event.eventID != null) {
                    _state.update {
                        it.copy(alreadyCreatedEvent = event.eventID)
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
                _dateSelected.value = event.newDay
                Log.d("EventActions", "DayChanged: ${event.newDay}")
            }

            is EventActions.SetUpStates -> {
                _state.value = _state.value.copy(
                    eventName = event.event.eventName,
                    date = event.event.date,
                    anyTimeTask = event.event.anyTimeEvent,
                    minTime = event.event.minTime,
                    maxTime = event.event.maxTime,
                    trackingMode = event.event.trackingMode,
                    eventColor = event.event.eventColor,
                    repeat = event.event.repeat,
                    position = event.event.position,
                    allowedApps = event.event.allowedApps,
                    blockedWebs = event.event.blockedWebs,
                    allowedWebs = event.event.allowedWebs,
                    customApps = event.event.customApps,
                )
            }

            is EventActions.ChangeEventPosition -> {
                viewModelScope.launch {
                    dao.updateEvent(event.event)
                }
            }

            is EventActions.SwitchHideMoreOptions -> {
                _state.update {
                    it.copy(sheetHideMoreOptions = event.value)
                }
            }
        }
    }
}