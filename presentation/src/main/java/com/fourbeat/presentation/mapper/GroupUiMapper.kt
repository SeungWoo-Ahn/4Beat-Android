package com.fourbeat.presentation.mapper

import com.fourbeat.domain.model.group.FeedPost
import com.fourbeat.domain.model.group.Group
import com.fourbeat.domain.model.group.GroupFeed
import com.fourbeat.domain.model.group.GroupFeedSlot
import com.fourbeat.domain.model.group.MyPostStatus
import com.fourbeat.presentation.model.group.FeedPostUiModel
import com.fourbeat.presentation.model.group.GroupFeedSlotUiModel
import com.fourbeat.presentation.model.group.GroupFeedUiData
import com.fourbeat.presentation.model.group.GroupUiModel
import java.text.SimpleDateFormat
import java.util.Locale

private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
private val timeFormatter = SimpleDateFormat("HH:mm", Locale.KOREA)

fun Group.toUiModel(): GroupUiModel =
    GroupUiModel(
        id = id,
        name = name,
        code = code,
        capacity = "${memberCount}명/${maxMemberCount}명",
    )

fun MyPostStatus.toAnnounce(): String =
    "· 오늘은 ${remainingPostCount}번 더 올릴 수 있어"

fun GroupFeed.toUiData(): GroupFeedUiData =
    GroupFeedUiData(
        date = date,
        previousDate = previousDate,
        nextDate = nextDate,
        slots = slots.map(GroupFeedSlot::toUiModel),
    )

fun GroupFeedSlot.toUiModel(): GroupFeedSlotUiModel =
    GroupFeedSlotUiModel(
        member = member,
        posts = posts.map(FeedPost::toUiModel),
    )

fun FeedPost.toUiModel(): FeedPostUiModel =
    FeedPostUiModel(
        id = id,
        song = song,
        videoSource = videoSource,
        comment = comment,
        createdAt = runCatching {
            val trimmed = createdAt.substringBefore('.')
            timeFormatter.format(isoFormatter.parse(trimmed)!!)
        }.getOrDefault(createdAt),
    )
