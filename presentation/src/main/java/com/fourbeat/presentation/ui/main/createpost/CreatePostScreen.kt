package com.fourbeat.presentation.ui.main.createpost

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.model.post.VideoSource
import com.fourbeat.presentation.theme.Gray100
import com.fourbeat.presentation.theme.Gray500
import com.fourbeat.presentation.theme.PrimaryColor
import com.fourbeat.presentation.theme.bold14
import com.fourbeat.presentation.theme.bold18
import com.fourbeat.presentation.theme.contentPadding
import com.fourbeat.presentation.theme.corderRadius
import com.fourbeat.presentation.theme.normal14
import com.fourbeat.presentation.ui.component.FourBeatButton
import com.fourbeat.presentation.ui.component.FourBeatLabel
import com.fourbeat.presentation.ui.component.FourBeatTextArea
import com.fourbeat.presentation.ui.component.NetworkImage
import com.fourbeat.presentation.ui.component.TitleTopBar
import com.fourbeat.presentation.ui.component.VideoPlayer
import com.fourbeat.presentation.ui.component.rememberExoPlayer
import com.fourbeat.presentation.ui.main.VIDEO_PATH_KEY
import com.fourbeat.presentation.ui.util.noRippleClickable
import java.io.File

@Composable
fun CreatePostRoute(
    modifier: Modifier = Modifier,
    backStackEntry: NavBackStackEntry?,
    navigateToGroupDetail: () -> Unit,
    navigateToCamera: () -> Unit,
    navigateToBack: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel(),
) {
    var launchPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onEvent(CreatePostEvent.OnCameraPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                CreatePostSideEffect.NavigateToBack -> navigateToBack()
                CreatePostSideEffect.CheckCameraPermission -> launchPermission = true
                CreatePostSideEffect.NavigateToCamera -> navigateToCamera()
                CreatePostSideEffect.NavigateToGroupDetail -> navigateToGroupDetail()
            }
        }
    }

    LaunchedEffect(backStackEntry) {
        backStackEntry
            ?.savedStateHandle
            ?.get<String>(VIDEO_PATH_KEY)
            ?.let { path ->
                val file = File(path)
                viewModel.onEvent(CreatePostEvent.OnVideoFileSelected(file))
                backStackEntry.savedStateHandle.remove<String>(VIDEO_PATH_KEY)
            }
    }

    LaunchedEffect(launchPermission) {
        if (launchPermission) {
            launchPermission = false
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    CreatePostScreen(
        modifier = modifier,
        uiState = viewModel.uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun CreatePostScreen(
    modifier: Modifier = Modifier,
    uiState: CreatePostUiState,
    onEvent: (CreatePostEvent) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TitleTopBar(
            title = "한 박자 올리기",
            onBack = { onEvent(CreatePostEvent.OnBackClicked) },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = contentPadding)
                .padding(bottom = contentPadding),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PrimaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(all = 12.dp),
                text = uiState.announce,
                color = PrimaryColor,
                style = bold14
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FourBeatLabel(text = "노래")
                SongInfoItem(song = uiState.song)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FourBeatLabel(text = "영상")
                VideoArea(
                    videoFile = uiState.videoFileInfo?.file,
                    onVideoBoxClicked = { onEvent(CreatePostEvent.OnVideoShotClicked) },
                    onVideoDeleted = { onEvent(CreatePostEvent.OnVideoDeleted) },
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FourBeatLabel(text = "코멘트")
                    Text(
                        text = uiState.commentLimit,
                        color = Gray500,
                        style = normal14
                    )
                }
                FourBeatTextArea(
                    value = uiState.comment,
                    placeholder = "감상을 작성해봐",
                    maxLength = CreatePostUiState.COMMENT_MAX_LENGTH,
                    onValueChange = { onEvent(CreatePostEvent.OnCommentChanged(it)) },
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            FourBeatButton(
                isLoading = uiState.isLoading,
                enabled = uiState.isLoading.not(),
                text = "올리기",
                onClick = { onEvent(CreatePostEvent.OnUploadClicked) },
            )
        }
    }
}

@Composable
private fun SongInfoItem(
    modifier: Modifier = Modifier,
    song: Song,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NetworkImage(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            url = song.albumImageUrl ?: "",
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = song.title, style = bold18)
            Text(text = song.artist, style = normal14, color = Gray500)
        }
    }
}

@Composable
private fun VideoArea(
    modifier: Modifier = Modifier,
    videoFile: File?,
    onVideoBoxClicked: () -> Unit,
    onVideoDeleted: () -> Unit,
) {
    if (videoFile != null) {
        VideoPreview(
            modifier = modifier,
            videoFile = videoFile,
            onVideoDeleted = onVideoDeleted,
        )
    } else {
        VideoEmpty(
            modifier = modifier,
            onVideoBoxClicked = onVideoBoxClicked,
        )
    }
}

@Composable
private fun VideoEmpty(
    modifier: Modifier = Modifier,
    onVideoBoxClicked: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(color = Gray100, shape = RoundedCornerShape(corderRadius))
            .noRippleClickable(onClick = onVideoBoxClicked),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.PlayArrow,
            contentDescription = "ic-play",
            tint = Gray500
        )
    }
}

@Composable
private fun VideoPreview(
    modifier: Modifier = Modifier,
    videoFile: File,
    onVideoDeleted: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(corderRadius)),
    ) {
        VideoPlayer(
            modifier = Modifier.fillMaxSize(),
            exoPlayer = rememberExoPlayer(),
            source = VideoSource.Local(videoFile),
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onVideoDeleted,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
            )
        }
    }
}

