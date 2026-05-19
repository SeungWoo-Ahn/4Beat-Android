package com.fourbeat.presentation.ui.component

import android.graphics.Matrix
import android.view.TextureView
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.fourbeat.domain.model.post.VideoSource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@OptIn(UnstableApi::class)
@EntryPoint
@InstallIn(SingletonComponent::class)
interface VideoCacheEntryPoint {
    fun dataSourceFactory(): DataSource.Factory
}

@OptIn(UnstableApi::class)
@Composable
fun rememberExoPlayer(): ExoPlayer = rememberExoPlayerPool(size = 1).first()

@OptIn(UnstableApi::class)
@Composable
fun rememberExoPlayerPool(size: Int = 3): List<ExoPlayer> {
    val context = LocalContext.current
    val dataSourceFactory = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            VideoCacheEntryPoint::class.java,
        ).dataSourceFactory()
    }
    val players = remember {
        List(size) {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                .build()
                .apply { repeatMode = ExoPlayer.REPEAT_MODE_ONE }
        }
    }
    DisposableEffect(Unit) {
        onDispose { players.forEach { it.release() } }
    }
    return players
}

@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    source: VideoSource,
    isActive: Boolean = true,
) {
    val uri = remember(source) {
        when (source) {
            is VideoSource.Local -> source.file.toUri()
            is VideoSource.Remote -> source.url.toUri()
        }
    }

    LaunchedEffect(uri, exoPlayer) {
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
    }

    LaunchedEffect(isActive, exoPlayer) {
        if (isActive) exoPlayer.play() else exoPlayer.pause()
    }

    val viewHolder = remember { TextureViewHolder() }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                viewHolder.view?.let { applyCenterCrop(it, videoSize) }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                viewHolder.view = this
                addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                    applyCenterCrop(v as TextureView, exoPlayer.videoSize)
                }
            }
        },
        update = { view ->
            exoPlayer.setVideoTextureView(view)
            applyCenterCrop(view, exoPlayer.videoSize)
        },
    )
}

private class TextureViewHolder {
    var view: TextureView? = null
}

private fun applyCenterCrop(view: TextureView, videoSize: VideoSize) {
    val viewWidth = view.width.toFloat()
    val viewHeight = view.height.toFloat()
    val videoWidth = videoSize.width.toFloat()
    val videoHeight = videoSize.height.toFloat()
    if (viewWidth <= 0f || viewHeight <= 0f || videoWidth <= 0f || videoHeight <= 0f) return

    val viewRatio = viewWidth / viewHeight
    val videoRatio = videoWidth / videoHeight
    val scaleX: Float
    val scaleY: Float
    if (videoRatio > viewRatio) {
        scaleX = videoRatio / viewRatio
        scaleY = 1f
    } else {
        scaleX = 1f
        scaleY = viewRatio / videoRatio
    }
    val matrix = Matrix()
    matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
    view.setTransform(matrix)
}
