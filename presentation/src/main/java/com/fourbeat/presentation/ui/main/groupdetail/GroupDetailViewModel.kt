package com.fourbeat.presentation.ui.main.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fourbeat.domain.usecase.group.ObserveGroupFeedUseCase
import com.fourbeat.domain.usecase.group.RefreshGroupFeedUseCase
import com.fourbeat.presentation.mapper.toUiData
import com.fourbeat.presentation.model.group.GroupFeedUiData
import com.fourbeat.presentation.navigation.MainScreen
import com.fourbeat.presentation.util.DateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val observeGroupFeedUseCase: ObserveGroupFeedUseCase,
    private val refreshGroupFeedUseCase: RefreshGroupFeedUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val groupId = savedStateHandle.toRoute<MainScreen.GroupDetail>().groupId
    private val currentDate = MutableStateFlow(DateProvider.today())
    private val adjacentFeeds = MutableStateFlow<Pair<GroupFeedUiData?, GroupFeedUiData?>>(null to null)
    private val isError = MutableStateFlow(false)
    private var scrollJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(
        currentDate.flatMapLatest { date -> observeGroupFeedUseCase(groupId, date) },
        adjacentFeeds,
        isError,
    ) { feed, (prev, next), error ->
        GroupDetailUiState(
            currentFeed = feed.toUiData(),
            previousFeed = prev,
            nextFeed = next,
            isError = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupDetailUiState(isLoading = true),
    )

    init {
        viewModelScope.launch {
            refreshGroupFeedUseCase(groupId, currentDate.value)
                .onSuccess { feed ->
                    launch { feed.previousDate?.let { prefetch(it, isPrevious = true) } }
                    launch { feed.nextDate?.let { prefetch(it, isPrevious = false) } }
                }
                .onFailure { isError.value = true }
        }
    }

    fun onEvent(event: GroupDetailEvent) {
        when (event) {
            GroupDetailEvent.OnScrollToPrev -> scrollToPrev()
            GroupDetailEvent.OnScrollToNext -> scrollToNext()
            GroupDetailEvent.OnRetry -> retry()
        }
    }

    private fun retry() {
        isError.value = false
        viewModelScope.launch {
            refreshGroupFeedUseCase(groupId, currentDate.value)
                .onSuccess { feed ->
                    launch { feed.previousDate?.let { prefetch(it, isPrevious = true) } }
                    launch { feed.nextDate?.let { prefetch(it, isPrevious = false) } }
                }
                .onFailure { isError.value = true }
        }
    }

    private fun scrollToPrev() {
        val current = uiState.value.currentFeed ?: return
        val prev = uiState.value.previousFeed ?: return
        adjacentFeeds.value = null to current
        currentDate.value = prev.date
        scrollJob?.cancel()
        scrollJob = viewModelScope.launch {
            refreshGroupFeedUseCase(groupId, prev.date)
            prev.previousDate?.let { prefetch(it, isPrevious = true) }
        }
    }

    private fun scrollToNext() {
        val current = uiState.value.currentFeed ?: return
        val next = uiState.value.nextFeed ?: return
        adjacentFeeds.value = current to null
        currentDate.value = next.date
        scrollJob?.cancel()
        scrollJob = viewModelScope.launch {
            refreshGroupFeedUseCase(groupId, next.date)
            next.nextDate?.let { prefetch(it, isPrevious = false) }
        }
    }

    private suspend fun prefetch(date: String, isPrevious: Boolean) {
        refreshGroupFeedUseCase(groupId, date)
            .onSuccess { feed ->
                adjacentFeeds.update { (prev, next) ->
                    if (isPrevious) {
                        feed.toUiData() to next
                    } else {
                        prev to feed.toUiData()
                    }
                }
            }
    }
}
