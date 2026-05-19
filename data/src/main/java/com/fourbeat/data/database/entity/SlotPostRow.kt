package com.fourbeat.data.database.entity

data class SlotPostRow(
    // slots 테이블 (LEFT JOIN 기준, 항상 non-null)
    val memberId: Long,
    val memberName: String,
    val memberNickname: String,
    val slotOrder: Int,
    val nextDate: String?,
    val previousDate: String?,
    // posts 테이블 (게시글 없을 때 null)
    val postId: Long?,
    val songTitle: String?,
    val songArtist: String?,
    val albumImageUrl: String?,
    val filePath: String?,
    val videoUrl: String?,
    val comment: String?,
    val createdAt: String?,
    val status: PostStatus?,
)
