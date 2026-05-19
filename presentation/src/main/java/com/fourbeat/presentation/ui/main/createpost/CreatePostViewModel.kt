package com.fourbeat.presentation.ui.main.createpost

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.fourbeat.domain.model.post.CreatePostRequest
import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.model.post.VideoFileInfo
import com.fourbeat.domain.usecase.group.GetGroupPostStatusUseCase
import com.fourbeat.domain.usecase.group.SubmitPostUseCase
import com.fourbeat.presentation.mapper.toAnnounce
import com.fourbeat.presentation.navigation.MainScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val submitPostUseCase: SubmitPostUseCase,
    private val getGroupPostStatusUseCase: GetGroupPostStatusUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<MainScreen.CreatePost>()
    private val song = Song(
        title = route.songTitle,
        artist = route.songArtist,
        albumImageUrl = route.songImageUrl,
    )

    var uiState by mutableStateOf(CreatePostUiState(song = song))
        private set

    private val _sideEffect = Channel<CreatePostSideEffect>()
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        /*
        * 그룹 내 나의 게시글 상태 보기
        * 오늘 작성할 수 있는 게시글 개수 표시
        * */
        viewModelScope.launch {
            getGroupPostStatusUseCase(route.groupId)
                .onSuccess { status ->
                    uiState = uiState.copy(announce = status.toAnnounce())
                }
        }
    }

    fun onEvent(event: CreatePostEvent) {
        when (event) {
            CreatePostEvent.OnBackClicked -> viewModelScope.launch {
                _sideEffect.send(CreatePostSideEffect.NavigateToBack)
            }
            CreatePostEvent.OnVideoShotClicked -> viewModelScope.launch {
                _sideEffect.send(CreatePostSideEffect.CheckCameraPermission)
            }
            is CreatePostEvent.OnCameraPermissionResult -> {
                if (event.granted) viewModelScope.launch {
                    _sideEffect.send(CreatePostSideEffect.NavigateToCamera)
                }
            }
            is CreatePostEvent.OnVideoFileSelected -> uiState = uiState.copy(
                videoFileInfo = VideoFileInfo(file = event.file)
            )
            CreatePostEvent.OnVideoDeleted -> {
                val file = uiState.videoFileInfo?.file
                uiState = uiState.copy(videoFileInfo = null)
                viewModelScope.launch(Dispatchers.IO) { file?.delete() }
            }
            is CreatePostEvent.OnCommentChanged -> uiState = uiState.copy(comment = event.comment)
            CreatePostEvent.OnUploadClicked -> upload()
        }
    }

    private fun upload() {
        uiState = uiState.copy(isLoading = true)
        viewModelScope.launch {
            submitPostUseCase(
                groupId = route.groupId,
                request = CreatePostRequest(
                    song = song,
                    comment = uiState.comment.ifBlank { null },
                    videoUrl = null,
                ),
                videoFileInfo = uiState.videoFileInfo,
            )
                .onSuccess {
                    _sideEffect.send(CreatePostSideEffect.NavigateToGroupDetail)
                }
                .onFailure {
                    uiState = uiState.copy(isLoading = false)
                }
        }
    }
}
