package com.fourbeat.presentation.ui.main.groupdetail

import com.fourbeat.presentation.model.group.GroupFeedUiData

data class GroupDetailUiState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val previousFeed: GroupFeedUiData? = null,
    val currentFeed: GroupFeedUiData? = null,
    val nextFeed: GroupFeedUiData? = null,
)

sealed interface GroupDetailEvent {
    data object OnScrollToPrev : GroupDetailEvent
    data object OnScrollToNext : GroupDetailEvent
    data object OnRetry : GroupDetailEvent
}
