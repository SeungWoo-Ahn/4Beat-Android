package com.fourbeat.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.fourbeat.data.database.entity.SlotEntity
import com.fourbeat.data.database.entity.SlotPostRow
import kotlinx.coroutines.flow.Flow

@Dao
interface SlotDao {
    @Upsert
    suspend fun upsertAll(slots: List<SlotEntity>)

    @Query("DELETE FROM slots WHERE groupId = :groupId AND date = :date")
    suspend fun deleteByGroupAndDate(groupId: Long, date: String)

    @Transaction
    suspend fun replaceByGroupAndDate(groupId: Long, date: String, slots: List<SlotEntity>) {
        deleteByGroupAndDate(groupId, date)
        upsertAll(slots)
    }

    @Query("SELECT slotOrder FROM slots WHERE groupId = :groupId AND date = :date AND memberId = :memberId")
    suspend fun getSlotOrder(groupId: Long, date: String, memberId: Long): Int?

    @Query("""
        SELECT s.memberId, s.memberName, s.memberNickname, s.slotOrder, s.nextDate, s.previousDate,
               p.id AS postId, p.songTitle, p.songArtist, p.albumImageUrl,
               p.filePath, p.videoUrl, p.comment, p.createdAt, p.status
        FROM slots s
        LEFT JOIN posts p ON p.memberId = s.memberId AND p.groupId = s.groupId AND p.date = s.date
        WHERE s.groupId = :groupId AND s.date = :date
        ORDER BY s.slotOrder ASC, p.createdAt ASC
    """)
    fun observeByGroupAndDate(groupId: Long, date: String): Flow<List<SlotPostRow>>
}
