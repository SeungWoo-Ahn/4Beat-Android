package com.fourbeat.presentation.ui.main.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fourbeat.presentation.theme.Gray200
import com.fourbeat.presentation.theme.Gray500
import com.fourbeat.presentation.theme.bold18
import com.fourbeat.presentation.theme.contentPadding
import com.fourbeat.presentation.theme.corderRadius
import com.fourbeat.presentation.theme.normal14
import com.fourbeat.presentation.ui.component.ErrorComponent
import com.fourbeat.presentation.ui.component.HomeTopBar
import com.fourbeat.presentation.ui.component.LoadingComponent

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    navigateToCreateGroup: () -> Unit,
    navigateToJoinGroupDialog: () -> Unit,
    navigateToGroupDetail: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                HomeSideEffect.NavigateToCreateGroup -> navigateToCreateGroup()
                HomeSideEffect.NavigateToJoinGroupDialog -> navigateToJoinGroupDialog()
                is HomeSideEffect.NavigateToGroupDetail -> navigateToGroupDetail(effect.groupId)
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(HomeEvent.OnRefresh)
    }

    HomeScreen(
        modifier = modifier,
        uiState = viewModel.uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        HomeTopBar(
            onPlusClick = { onEvent(HomeEvent.OnPlusIconClicked) },
            onHashClick = { onEvent(HomeEvent.OnHashIconClicked) },
        )
        when (uiState) {
            HomeUiState.Loading -> LoadingComponent()
            HomeUiState.Error -> ErrorComponent(onRefresh = { onEvent(HomeEvent.OnRefresh) })
            is HomeUiState.Success -> Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(all = contentPadding)
            ) {
                HomeGroupList(
                    uiState = uiState,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun HomeGroupList(
    modifier: Modifier = Modifier,
    uiState: HomeUiState.Success,
    onEvent: (HomeEvent) -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(state = scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        uiState.groups.fastForEach { group ->
            HomeGroupItem(
                name = group.name,
                capacity = group.capacity,
                onClick = { onEvent(HomeEvent.OnGroupItemClicked(group.id)) },
            )
        }
    }
}

@Composable
private fun HomeGroupItem(
    modifier: Modifier = Modifier,
    name: String,
    capacity: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 0.5.dp,
                color = Gray200,
                shape = RoundedCornerShape(size = corderRadius)
            )
            .padding(
                horizontal = 20.dp,
                vertical = 36.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = name,
            style = bold18
        )
        Text(
            text = capacity,
            color = Gray500,
            style = normal14,
        )
    }
}
