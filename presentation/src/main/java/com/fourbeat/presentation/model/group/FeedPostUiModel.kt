package com.fourbeat.presentation.model.group

import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.model.post.VideoSource

data class FeedPostUiModel(
    val id: Long,
    val song: Song,
    val videoSource: VideoSource?,
    val comment: String?,
    val createdAt: String,
)
