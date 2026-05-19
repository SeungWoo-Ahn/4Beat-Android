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

fun Group.toUiModel(): GroupUiModel =
    GroupUiModel(
        id = id,
        name = name,
        code = code,
        capacity = "${memberCount}명/${maxMemberCount}명",
    )

fun MyPostStatus.toMessage(): String =
    if (canPost) {
        "오늘 ${totalPostLimit}회 중에 ${remainingPostCount}회 남았어요"
    } else {
        "오늘 ${totalPostLimit}회의 할당량을 모두 소진했어요"
    }

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
        createdAt = createdAt,
    )
