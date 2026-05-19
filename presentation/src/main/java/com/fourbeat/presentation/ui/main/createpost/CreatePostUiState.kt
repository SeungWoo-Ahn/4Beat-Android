package com.fourbeat.presentation.ui.main.createpost

import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.model.post.VideoFileInfo

data class CreatePostUiState(
    val song: Song,
    val announce: String = "",
    val videoFileInfo: VideoFileInfo? = null,
    val comment: String = "",
    val isLoading: Boolean = false,
) {
    val commentLimit get() = "${comment.length}/${COMMENT_MAX_LENGTH}"

    companion object {
        const val COMMENT_MAX_LENGTH = 50
    }
}

sealed interface CreatePostEvent {
    data object OnBackClicked : CreatePostEvent
    data object OnVideoShotClicked : CreatePostEvent
    data class OnCameraPermissionResult(val granted: Boolean) : CreatePostEvent
    data class OnVideoFileSelected(val file: java.io.File) : CreatePostEvent
    data object OnVideoDeleted : CreatePostEvent
    data class OnCommentChanged(val comment: String) : CreatePostEvent
    data object OnUploadClicked : CreatePostEvent
}

sealed interface CreatePostSideEffect {
    data object NavigateToBack : CreatePostSideEffect
    data object CheckCameraPermission : CreatePostSideEffect
    data object NavigateToCamera : CreatePostSideEffect
    data object NavigateToGroupDetail : CreatePostSideEffect
}
