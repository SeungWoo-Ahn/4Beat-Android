package com.fourbeat.data.mapper

import com.fourbeat.data.database.entity.PostEntity
import com.fourbeat.data.database.entity.PostStatus
import com.fourbeat.data.database.entity.SongInfo
import com.fourbeat.data.network.dto.post.CreatePostRequestBody
import com.fourbeat.data.network.dto.post.FileUploadUrlRequestBody
import com.fourbeat.data.network.dto.post.FileUploadUrlResponse
import com.fourbeat.data.network.dto.post.PostResponse
import com.fourbeat.data.network.dto.post.PostSongDto
import com.fourbeat.domain.model.group.FeedPost
import com.fourbeat.domain.model.post.CreatePostRequest
import com.fourbeat.domain.model.post.FileUploadUrl
import com.fourbeat.domain.model.post.FileUploadUrlRequest
import com.fourbeat.domain.model.post.Post
import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.model.post.VideoSource
import com.fourbeat.domain.model.user.User

fun PostResponse.toDomain(): Post =
    Post(
        id = id,
        author = author.toDomain(),
        song = song.toDomain(),
        videoUrl = videoUrl,
        comment = comment,
        createdAt = createdAt,
    )

fun PostSongDto.toDomain(): Song =
    Song(
        title = title,
        artist = artist,
        albumImageUrl = imageUrl,
    )

fun Song.toDto(): PostSongDto =
    PostSongDto(
        title = title,
        artist = artist,
        imageUrl = albumImageUrl,
    )

fun CreatePostRequest.asBody(): CreatePostRequestBody =
    CreatePostRequestBody(
        song = song.toDto(),
        comment = comment,
        videoUrl = videoUrl,
    )

fun CreatePostRequest.toEntity(
    tempId: Long,
    groupId: Long,
    today: String,
    member: User,
    filePath: String?,
): PostEntity = PostEntity(
    id = tempId,
    groupId = groupId,
    date = today,
    memberId = member.id,
    song = SongInfo(
        title = song.title,
        artist = song.artist,
        albumImageUrl = song.albumImageUrl,
    ),
    filePath = filePath,
    videoUrl = null,
    comment = comment,
    status = PostStatus.PENDING,
)

fun FileUploadUrlResponse.toDomain(): FileUploadUrl =
    FileUploadUrl(
        uploadUrl = uploadUrl,
        videoUrl = videoUrl,
    )

fun FileUploadUrlRequest.asBody(): FileUploadUrlRequestBody =
    FileUploadUrlRequestBody(
        fileName = fileName,
        contentType = "video/mp4",
    )

fun PostEntity.toDomain(videoSource: VideoSource?): FeedPost =
    FeedPost(
        id = id,
        song = Song(title = song.title, artist = song.artist, albumImageUrl = song.albumImageUrl),
        videoSource = videoSource,
        comment = comment,
        createdAt = createdAt,
    )
