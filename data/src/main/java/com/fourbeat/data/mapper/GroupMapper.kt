package com.fourbeat.data.mapper

import com.fourbeat.data.database.entity.PostEntity
import com.fourbeat.data.database.entity.PostStatus
import com.fourbeat.data.database.entity.SlotEntity
import com.fourbeat.data.database.entity.SlotPostRow
import com.fourbeat.data.database.entity.SongInfo
import com.fourbeat.data.network.dto.group.CreateGroupRequestBody
import com.fourbeat.data.network.dto.group.GroupFeedResponse
import com.fourbeat.data.network.dto.group.GroupResponse
import com.fourbeat.data.network.dto.group.MyPostStatusResponse
import com.fourbeat.domain.model.group.CreateGroupRequest
import com.fourbeat.domain.model.group.FeedPost
import com.fourbeat.domain.model.group.Group
import com.fourbeat.domain.model.group.GroupFeed
import com.fourbeat.domain.model.group.GroupFeedSlot
import com.fourbeat.domain.model.group.MyPostStatus
import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.model.post.VideoSource
import com.fourbeat.domain.model.user.User
import java.io.File

fun GroupResponse.toDomain(): Group =
    Group(
        id = id,
        name = name,
        code = code,
        maxMemberCount = maxMemberCount,
        memberCount = memberCount,
    )

fun MyPostStatusResponse.toDomain(): MyPostStatus =
    MyPostStatus(
        remainingPostCount = remainingPostCount,
        totalPostLimit = totalPostLimit,
        canPost = canPost,
    )

fun CreateGroupRequest.asBody(): CreateGroupRequestBody =
    CreateGroupRequestBody(
        name = name,
        maxMemberCount = maxMemberCount.value,
    )

fun GroupFeedResponse.toSlotEntities(groupId: Long): List<SlotEntity> =
    slots.map { slot ->
        SlotEntity(
            groupId = groupId,
            date = date,
            memberId = slot.member.id,
            memberName = slot.member.name,
            memberNickname = slot.member.nickname,
            slotOrder = slot.order,
            nextDate = nextDate,
            previousDate = previousDate,
        )
    }

fun GroupFeedResponse.toPostEntities(groupId: Long): List<PostEntity> =
    slots.flatMap { slot ->
        slot.posts.map { post ->
            PostEntity(
                id = post.id,
                groupId = groupId,
                date = date,
                memberId = slot.member.id,
                song = SongInfo(
                    title = post.song.title,
                    artist = post.song.artist,
                    albumImageUrl = post.song.imageUrl,
                ),
                filePath = null,
                videoUrl = post.videoUrl,
                comment = post.comment,
                createdAt = post.createdAt,
                status = PostStatus.STABLE,
            )
        }
    }

fun List<SlotPostRow>.toGroupFeed(date: String): GroupFeed {
    val firstRow = firstOrNull()
    val nextDate = firstRow?.nextDate
    val previousDate = firstRow?.previousDate
    val slots = groupBy { it.memberId }
        .map { (memberId, rows) ->
            val first = rows.first()
            GroupFeedSlot(
                order = first.slotOrder,
                member = User(id = memberId, name = first.memberName, nickname = first.memberNickname),
                posts = rows
                    .filter { it.postId != null }
                    .map { it.toFeedPost() }
                    .sorted(),
            )
        }
        .sorted()
    return GroupFeed(date = date, nextDate = nextDate, previousDate = previousDate, slots = slots)
}

fun buildGroupFeed(
    date: String,
    slotEntities: List<SlotEntity>,
    postEntities: List<PostEntity>,
): GroupFeed {
    val postsByMemberId = postEntities.groupBy { it.memberId }
    val slots = slotEntities
        .map { slot ->
            GroupFeedSlot(
                order = slot.slotOrder,
                member = User(slot.memberId, slot.memberName, slot.memberNickname),
                posts = postsByMemberId[slot.memberId]
                    ?.map { post ->
                        FeedPost(
                            id = post.id,
                            song = Song(post.song.title, post.song.artist, post.song.albumImageUrl),
                            videoSource = post.videoUrl?.let { VideoSource.Remote(it) },
                            comment = post.comment,
                            createdAt = post.createdAt,
                        )
                    }
                    ?.sorted()
                    ?: emptyList(),
            )
        }
        .sorted()
    return GroupFeed(
        date = date,
        nextDate = slotEntities.firstOrNull()?.nextDate,
        previousDate = slotEntities.firstOrNull()?.previousDate,
        slots = slots,
    )
}

private fun SlotPostRow.toFeedPost(): FeedPost {
    val videoSource = when (status) {
        PostStatus.PENDING -> filePath
            ?.let { File(it) }
            ?.takeIf { it.exists() }
            ?.let { VideoSource.Local(it) }
        PostStatus.STABLE -> videoUrl?.let { VideoSource.Remote(it) }
        else -> null
    }
    return FeedPost(
        id = postId!!,
        song = Song(title = songTitle!!, artist = songArtist!!, albumImageUrl = albumImageUrl),
        videoSource = videoSource,
        comment = comment,
        createdAt = createdAt!!,
    )
}
