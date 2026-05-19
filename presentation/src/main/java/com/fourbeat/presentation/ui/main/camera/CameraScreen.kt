package com.fourbeat.presentation.ui.main.camera

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fourbeat.presentation.theme.contentPadding
import timber.log.Timber

@Composable
fun CameraRoute(
    navigateBack: (filePath: String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    /*
    * 화면 가로 방향 고정
    * */
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation =
                originalOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    val previewView = remember { PreviewView(context) }
    val uiState = viewModel.uiState
    val videoCapture = remember(uiState.cameraLens) {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .setTargetVideoEncodingBitRate(1_000_000)
            .build()
        VideoCapture.withOutput(recorder)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CameraSideEffect.SaveVideoAndNavigateBack -> navigateBack(effect.filePath)
            }
        }
    }

    /*
    * 전면/후면 카메라, VideoCapture 변경될 때마다 트리거
    * CamerProvider lifecycle에 바인딩
    * UseCase: Preview (촬영 중인 화면 보기), videoCapture (영상 촬영)
    * */
    LaunchedEffect(uiState.cameraLens, videoCapture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().apply {
            surfaceProvider = previewView.surfaceProvider
        }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner = lifecycleOwner,
                cameraSelector = CameraSelector.Builder().requireLensFacing(uiState.cameraLens).build(),
                preview, videoCapture
            )
            viewModel.onEvent(CameraEvent.OnVideoCaptureReady(videoCapture))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    CameraScreen(
        uiState = viewModel.uiState,
        previewView = previewView,
        onEvent = viewModel::onEvent,
    )
}

private val RecordButtonSize = 72.dp

@Composable
private fun CameraScreen(
    uiState: CameraUiState,
    previewView: PreviewView,
    onEvent: (CameraEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView },
        )
        if (uiState.isRecording) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = "${uiState.remainingSeconds}",
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(all = contentPadding),
            onClick = { onEvent(CameraEvent.OnCameraFlipClicked) },
            enabled = !uiState.isRecording,
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White,
            )
        }

        RecordButton(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(all = contentPadding),
            isRecording = uiState.isRecording,
            onClick = { onEvent(CameraEvent.OnRecordButtonClicked) },
        )
    }
}

@Composable
private fun RecordButton(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(RecordButtonSize)
            .clickable(onClick = onClick)
            .border(width = 4.dp, color = Color.White, shape = CircleShape)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(color = Color.Red, shape = RoundedCornerShape(6.dp)),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Red, shape = CircleShape),
            )
        }
    }
}
