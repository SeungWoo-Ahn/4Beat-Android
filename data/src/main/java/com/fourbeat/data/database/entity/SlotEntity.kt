package com.fourbeat.data.database.entity

import androidx.room.Entity

@Entity(tableName = "slots", primaryKeys = ["groupId", "date", "memberId"])
data class SlotEntity(
    val groupId: Long,
    val date: String,
    val memberId: Long,
    val memberName: String,
    val memberNickname: String,
    val slotOrder: Int,
    val nextDate: String?,
    val previousDate: String?,
)
