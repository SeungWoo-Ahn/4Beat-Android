package com.fourbeat.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(
    tableName = "posts",
    indices = [Index(value = ["groupId", "date"])],
)
data class PostEntity(
    @PrimaryKey val id: Long,
    val groupId: Long,
    val date: String,
    val memberId: Long,
    @Embedded val song: SongInfo,
    val filePath: String?,
    val videoUrl: String?,
    val comment: String?,
    val createdAt: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA).format(Date()),
    val status: PostStatus,
)
