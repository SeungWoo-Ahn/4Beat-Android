package com.fourbeat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.fourbeat.data.database.entity.PostEntity
import com.fourbeat.data.database.entity.PostStatus

@Dao
interface PostDao {
    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Query("DELETE FROM posts WHERE groupId = :groupId AND date = :date AND status = 'STABLE'")
    suspend fun deleteStableByGroupAndDate(groupId: Long, date: String)

    @Transaction
    suspend fun replaceStable(groupId: Long, date: String, posts: List<PostEntity>) {
        deleteStableByGroupAndDate(groupId, date)
        upsertAll(posts)
    }

    @Query("UPDATE posts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: PostStatus)

    @Transaction
    suspend fun confirmPost(tempId: Long, realEntity: PostEntity) {
        delete(tempId)
        insert(realEntity)
    }

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getById(id: Long): PostEntity?
}
