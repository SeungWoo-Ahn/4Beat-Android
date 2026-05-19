package com.fourbeat.presentation.ui.main.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fourbeat.domain.model.group.Group
import com.fourbeat.domain.usecase.group.GetMyGroupsUseCase
import com.fourbeat.presentation.mapper.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMyGroupsUseCase: GetMyGroupsUseCase,
) : ViewModel() {
    var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    private val _sideEffect = Channel<HomeSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.OnPlusIconClicked -> onPlusIconClicked()
            is HomeEvent.OnHashIconClicked -> onHashIconClicked()
            is HomeEvent.OnRefresh -> loadMyGroups()
            is HomeEvent.OnGroupItemClicked -> onGroupItemClicked(event.groupId)
        }
    }

    /**
     * 내 그룹 리스트 조회
     * 성공 시, uiState 업데이트
     * 실패 시, 에러 메세지 보여주기 (TODO)
     * */
    private fun loadMyGroups() {
        viewModelScope.launch {
            uiState = HomeUiState.Loading
            getMyGroupsUseCase()
                .onSuccess { groups ->
                    uiState = HomeUiState.Success(groups.map(Group::toUiModel))
                }
                .onFailure {
                    uiState = HomeUiState.Error
                }
        }
    }

    /*
    * 그룹 생성 화면 이동
    * */
    private fun onPlusIconClicked() {
        viewModelScope.launch {
            _sideEffect.send(HomeSideEffect.NavigateToCreateGroup)
        }
    }

    /*
    * 그룹 참여 Dialog 이동
    * */
    private fun onHashIconClicked() {
        viewModelScope.launch {
            _sideEffect.send(HomeSideEffect.NavigateToJoinGroupDialog)
        }
    }

    /*
    * 그룹 상세 화면 이동
    * */
    private fun onGroupItemClicked(groupId: Long) {
        viewModelScope.launch {
            _sideEffect.send(HomeSideEffect.NavigateToGroupDetail(groupId))
        }
    }
}
