package com.fourbeat.presentation.ui.main.creategroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fourbeat.domain.model.group.GroupMemberCount
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.contentPadding
import com.fourbeat.presentation.theme.medium32
import com.fourbeat.presentation.ui.component.FourBeatButton
import com.fourbeat.presentation.ui.component.FourBeatLabel
import com.fourbeat.presentation.ui.component.FourBeatSpacer
import com.fourbeat.presentation.ui.component.FourBeatTextField
import com.fourbeat.presentation.ui.component.GroupMemberChip
import com.fourbeat.presentation.ui.component.TitleTopBar

@Composable
fun CreateGroupRoute(
    modifier: Modifier = Modifier,
    navigateToGroupDetail: (Long) -> Unit,
    navigateToBack: () -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CreateGroupSideEffect.NavigateToGroupDetail -> navigateToGroupDetail(effect.groupId)
                CreateGroupSideEffect.NavigateToBack -> navigateToBack()
            }
        }
    }

    CreateGroupScreen(
        modifier = modifier,
        uiState = viewModel.uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun CreateGroupScreen(
    modifier: Modifier = Modifier,
    uiState: CreateGroupUiState,
    onEvent: (CreateGroupEvent) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TitleTopBar(
            title = "방 만들기",
            onBack = { onEvent(CreateGroupEvent.OnBackClicked) }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(all = contentPadding),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("어떤 방으로 부를까")
                    withStyle(SpanStyle(color = PrimaryColor)) { append("?") }
                },
                style = medium32,
            )
            FourBeatTextField(
                value = uiState.name,
                label = "방 이름",
                placeholder = "방 이름을 알려주세요",
                maxLength = CreateGroupUiState.NAME_MAX_LENGTH,
                imeAction = ImeAction.Done,
                onValueChange = { value -> onEvent(CreateGroupEvent.OnNameChanged(value)) }
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FourBeatLabel(text = "인원 수")
                GroupMemberChipList(
                    selectedMemberCount = uiState.selectedMemberCount,
                    onSelected = { memberCount ->
                        onEvent(CreateGroupEvent.OnMemberCountSelected(memberCount))
                    }
                )
            }
            FourBeatSpacer(modifier = Modifier.weight(1f))
            FourBeatButton(
                isLoading = uiState.isLoading,
                enabled = uiState.isValid,
                text = "만들기",
                onClick = { onEvent(CreateGroupEvent.OnCreateButtonClicked) }
            )
        }
    }
}

@Composable
private fun GroupMemberChipList(
    modifier: Modifier = Modifier,
    selectedMemberCount: GroupMemberCount?,
    onSelected: (GroupMemberCount) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GroupMemberCount.entries.fastForEach { memberCount ->
            GroupMemberChip(
                modifier = Modifier.weight(1f),
                value = memberCount.value,
                isSelected = selectedMemberCount == memberCount,
                onSelected = { onSelected(memberCount) },
            )
        }
    }
}
