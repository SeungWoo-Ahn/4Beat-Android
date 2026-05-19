package com.fourbeat.presentation.ui.main.groupdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import coil3.compose.AsyncImage
import com.fourbeat.presentation.model.group.FeedPostUiModel
import com.fourbeat.presentation.model.group.GroupFeedSlotUiModel
import com.fourbeat.presentation.theme.Gray100
import com.fourbeat.presentation.theme.White
import com.fourbeat.presentation.theme.bold14
import com.fourbeat.presentation.theme.normal14
import com.fourbeat.presentation.ui.component.VideoPlayer

@Composable
fun GroupDetailSlotItem(
    modifier: Modifier = Modifier,
    slot: GroupFeedSlotUiModel,
    isActive: Boolean,
    exoPlayer: ExoPlayer?,
) {
    val pagerState = rememberPagerState { slot.posts.size }
    val settledPost = slot.posts.getOrNull(pagerState.settledPage)
    val videoSource = settledPost?.videoSource

    Box(modifier = modifier.clipToBounds()) {
        // Layer 1: Background
        when {
            exoPlayer != null && videoSource != null -> VideoPlayer(
                modifier = Modifier.fillMaxSize(),
                exoPlayer = exoPlayer,
                source = videoSource,
                isActive = isActive,
            )
            settledPost != null -> AsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = settledPost.song.albumImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            else -> Box(modifier = Modifier.fillMaxSize().background(Gray100))
        }

        // Layer 2: Dim overlay (강도는 영상/이미지/빈 슬롯 여부에 따라 조정)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = if (videoSource != null) 0.15f else 0.3f))
        )

        // Layer 3: HorizontalPager — 복수 게시글 간 좌우 스와이프 + 하단 게시글 정보
        if (slot.posts.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                PostInfoOverlay(post = slot.posts[page])
            }
        }

        // Layer 4: 멤버 정보(항상 고정) + 빈 슬롯 메시지
        Box(modifier = Modifier.matchParentSize().padding(12.dp)) {
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(text = slot.member.nickname, style = bold14, color = White)
                    Text(text = slot.member.name, style = normal14, color = White.copy(alpha = 0.7f))
                }
                settledPost?.let {
                    Text(text = it.createdAt, style = normal14, color = White.copy(alpha = 0.7f))
                }
            }

            if (slot.posts.isEmpty()) {
                Text(
                    text = "아직 게시글이 없어요",
                    modifier = Modifier.align(Alignment.Center),
                    style = normal14,
                    color = White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun PostInfoOverlay(post: FeedPostUiModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            post.comment?.let {
                Text(
                    text = it,
                    style = normal14,
                    color = White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
            } ?: Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AsyncImage(
                    model = post.song.albumImageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = post.song.title,
                    style = bold14,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = post.song.artist,
                    style = normal14,
                    color = White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
