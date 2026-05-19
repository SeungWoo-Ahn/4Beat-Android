package com.fourbeat.presentation.ui.main.groupdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import com.fourbeat.presentation.model.group.GroupFeedUiData
import com.fourbeat.presentation.ui.component.ErrorComponent
import com.fourbeat.presentation.ui.component.rememberExoPlayerPool
import com.fourbeat.presentation.ui.main.groupdetail.header.GroupDetailHeader

@Composable
fun GroupDetailRoute(
    modifier: Modifier = Modifier,
    navigateToSelectSong: (Long) -> Unit,
    showGroupCodeDialog: (String) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    GroupDetailScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        navigateToSelectSong = navigateToSelectSong,
        showGroupCodeDialog = showGroupCodeDialog,
    )
}

@Composable
private fun GroupDetailScreen(
    modifier: Modifier = Modifier,
    uiState: GroupDetailUiState,
    onEvent: (GroupDetailEvent) -> Unit,
    navigateToSelectSong: (Long) -> Unit,
    showGroupCodeDialog: (String) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        GroupDetailHeader(
            navigateToSelectSong = navigateToSelectSong,
            showGroupCodeDialog = showGroupCodeDialog,
        )
        Box(modifier = Modifier.weight(1f)) {
            GroupDetailFeed(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onEvent = onEvent,
            )
            if (uiState.isError) {
                ErrorComponent(
                    modifier = Modifier.fillMaxSize(),
                    onRefresh = { onEvent(GroupDetailEvent.OnRetry) },
                )
            }
        }
    }
}

@Composable
private fun GroupDetailFeed(
    modifier: Modifier = Modifier,
    uiState: GroupDetailUiState,
    onEvent: (GroupDetailEvent) -> Unit,
) {
    // ExoPlayer 3개 — 화면 전체에서 공유, 슬롯 위치(0/1/2)에 고정 할당
    val exoPlayers = rememberExoPlayerPool()

    val pagerState = rememberPagerState(initialPage = 1) { 3 }

    LaunchedEffect(uiState.currentFeed?.date) {
        if (pagerState.currentPage != 1) pagerState.scrollToPage(1)
    }

    LaunchedEffect(pagerState.settledPage) {
        when (pagerState.settledPage) {
            2 -> onEvent(GroupDetailEvent.OnScrollToPrev)
            0 -> onEvent(GroupDetailEvent.OnScrollToNext)
        }
    }

    val previousFeed by rememberUpdatedState(uiState.previousFeed)
    val nextFeed by rememberUpdatedState(uiState.nextFeed)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                when {
                    available.y < 0 && previousFeed == null -> available
                    available.y > 0 && nextFeed == null -> available
                    else -> Offset.Zero
                }
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.nestedScroll(nestedScrollConnection),
        userScrollEnabled = !uiState.isLoading,
    ) { page ->
        val feed = when (page) {
            0 -> uiState.nextFeed
            1 -> uiState.currentFeed
            2 -> uiState.previousFeed
            else -> null
        }
        if (feed != null) {
            GroupDetailDatePage(
                feed = feed,
                isActive = page == 1,
                exoPlayers = exoPlayers,
            )
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun GroupDetailDatePage(
    modifier: Modifier = Modifier,
    feed: GroupFeedUiData,
    isActive: Boolean,
    exoPlayers: List<ExoPlayer>,
) {
    val slotGroups = remember(feed.slots) { feed.slots.chunked(3) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val slotHeight = maxHeight / 3

        key(feed.date) {
            val pagerState = rememberPagerState { slotGroups.size }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val group = slotGroups.getOrNull(page) ?: return@HorizontalPager
                val isGroupActive = isActive && page == pagerState.settledPage
                Column(modifier = Modifier.fillMaxSize()) {
                    group.forEachIndexed { index, slot ->
                        GroupDetailSlotItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(slotHeight),
                            slot = slot,
                            isActive = isGroupActive,
                            exoPlayer = exoPlayers.getOrNull(index),
                        )
                    }
                }
            }
        }
    }
}
