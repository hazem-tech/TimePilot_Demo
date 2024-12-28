package com.timepilot.demo

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
    private val _events = _dateSelected.flatMapLatest { newDate ->
        dao.getEvents(newDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val state = combine(_state, _dateSelected, _events) { state, dateSelected, events ->
        state.copy(
            allEvent = events,
            daySelected = dateSelected
        )
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
            EventActions.SaveEvent -> {
                _state.update {
                    it.copy(isPartialSheet = false, isForcedSheet = false, isFullSheet = false)
                }
                val newEvent = Event(
                    eventName = state.value.eventName,
                    date = state.value.date,
                    minTime = state.value.minTime,
                    maxTime = state.value.maxTime,
                    trackingMode = state.value.trackingMode,
                    anyTimeEvent = state.value.anyTimeTask,
                    eventColor = state.value.eventColor,
                    repeats = state.value.repeats
                )
                viewModelScope.launch {
                    dao.upsertEvent(newEvent)
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
            }
            is EventActions.SetMaxTime -> {
                _state.update {
                    it.copy(maxTime = event.maxTime)
                }
            }
            is EventActions.SetTrackingMode -> {
                _state.update {
                    it.copy(trackingMode = event.trackingMode)
                }
            }
            is EventActions.ChangeAnytime -> {
                _state.update {
                    it.copy(anyTimeTask = event.anyTimeTask)
                }
            }
            is EventActions.SetRepeat -> {
                _state.update {
                    it.copy(repeats = event.frequencyDaysList)
                }
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
                TODO()
            }

            EventActions.HideSheet -> {
                _state.update {
                    it.copy(isPartialSheet = false, isForcedSheet = false, isFullSheet = false)
                }
            }
            EventActions.ShowFullSheet -> {
                _state.update {
                    it.copy(isPartialSheet = false, isForcedSheet = false, isFullSheet = true)
                }
            }
            EventActions.ShowForceFullSheet -> {
                _state.update {
                    it.copy(isPartialSheet = false, isForcedSheet = true, isFullSheet = false)
                }
            }
            EventActions.ShowPartialSheet -> {
                _state.update {
                    it.copy(isPartialSheet = true, isForcedSheet = false, isFullSheet = false)
                }
            }

            is EventActions.ChangeDay -> {
                _dateSelected.value = event.newDay
            }
        }
    }
}