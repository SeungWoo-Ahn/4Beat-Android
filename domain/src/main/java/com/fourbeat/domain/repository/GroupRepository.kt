package com.fourbeat.domain.repository

import com.fourbeat.domain.model.group.CreateGroupRequest
import com.fourbeat.domain.model.group.Group
import com.fourbeat.domain.model.group.GroupFeed
import com.fourbeat.domain.model.group.MyPostStatus
import com.fourbeat.domain.model.post.CreatePostRequest
import com.fourbeat.domain.model.post.Post
import com.fourbeat.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface GroupRepository {
    suspend fun createGroup(request: CreateGroupRequest): Group
    suspend fun getMyGroups(): List<Group>
    suspend fun joinGroup(code: String): Group
    suspend fun getGroupInfo(groupId: Long): Group
    suspend fun getGroupPostStatus(groupId: Long): MyPostStatus
    suspend fun createPost(groupId: Long, request: CreatePostRequest): Post
    fun observeGroupFeed(groupId: Long, date: String): Flow<GroupFeed>
    suspend fun refreshGroupFeed(groupId: Long, date: String): GroupFeed
    suspend fun insertOptimisticPost(
        groupId: Long,
        member: User,
        request: CreatePostRequest,
        filePath: String?,
    ): Long
    suspend fun rollbackPost(tempId: Long)
    suspend fun confirmPost(tempId: Long, post: Post)
}
