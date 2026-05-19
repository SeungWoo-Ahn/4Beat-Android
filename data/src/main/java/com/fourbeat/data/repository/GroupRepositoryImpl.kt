package com.fourbeat.data.repository

import com.fourbeat.data.database.dao.PostDao
import com.fourbeat.data.database.dao.SlotDao
import com.fourbeat.data.database.entity.PostStatus
import com.fourbeat.data.datasource.group.GroupDataSource
import com.fourbeat.data.mapper.asBody
import com.fourbeat.data.mapper.toDomain
import com.fourbeat.data.mapper.toEntity
import com.fourbeat.data.mapper.buildGroupFeed
import com.fourbeat.data.mapper.toGroupFeed
import com.fourbeat.data.mapper.toPostEntities
import com.fourbeat.data.mapper.toSlotEntities
import com.fourbeat.data.network.dto.group.GroupResponse
import com.fourbeat.domain.model.group.CreateGroupRequest
import com.fourbeat.domain.model.group.Group
import com.fourbeat.domain.model.group.GroupFeed
import com.fourbeat.domain.model.group.MyPostStatus
import com.fourbeat.domain.model.post.CreatePostRequest
import com.fourbeat.domain.model.post.Post
import com.fourbeat.domain.model.user.User
import com.fourbeat.domain.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDataSource: GroupDataSource,
    private val postDao: PostDao,
    private val slotDao: SlotDao,
) : GroupRepository {

    override suspend fun createGroup(request: CreateGroupRequest): Group =
        groupDataSource.createGroup(request.asBody()).toDomain()

    override suspend fun getMyGroups(): List<Group> =
        groupDataSource.getMyGroups().map(GroupResponse::toDomain)

    override suspend fun joinGroup(code: String): Group =
        groupDataSource.joinGroup(code).toDomain()

    override suspend fun getGroupInfo(groupId: Long): Group =
        groupDataSource.getGroupInfo(groupId).toDomain()

    override suspend fun getGroupPostStatus(groupId: Long): MyPostStatus =
        groupDataSource.getGroupPostStatus(groupId).toDomain()

    override suspend fun createPost(groupId: Long, request: CreatePostRequest): Post =
        groupDataSource.createPost(groupId = groupId, body = request.asBody()).toDomain()

    override fun observeGroupFeed(groupId: Long, date: String): Flow<GroupFeed> =
        slotDao.observeByGroupAndDate(groupId, date)
            .map { rows -> rows.toGroupFeed(date) }
            .flowOn(Dispatchers.IO)

    override suspend fun refreshGroupFeed(groupId: Long, date: String): GroupFeed {
        val response = groupDataSource.getGroupFeed(groupId, date)
        val slotEntities = response.toSlotEntities(groupId)
        val postEntities = response.toPostEntities(groupId)
        slotDao.replaceByGroupAndDate(groupId, date, slotEntities)
        postDao.replaceStable(groupId, date, postEntities)
        return buildGroupFeed(date, slotEntities, postEntities)
    }

    override suspend fun insertOptimisticPost(
        groupId: Long,
        member: User,
        request: CreatePostRequest,
        filePath: String?,
    ): Long {
        val tempId = UUID.randomUUID().mostSignificantBits.or(Long.MIN_VALUE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
        postDao.insert(
            request.toEntity(
                tempId = tempId,
                groupId = groupId,
                today = today,
                member = member,
                filePath = filePath,
            )
        )
        return tempId
    }

    override suspend fun rollbackPost(tempId: Long) {
        postDao.delete(tempId)
    }

    override suspend fun confirmPost(tempId: Long, post: Post) {
        val pending = postDao.getById(tempId) ?: return
        postDao.confirmPost(
            tempId = tempId,
            realEntity = pending.copy(
                id = post.id,
                videoUrl = post.videoUrl,
                filePath = null,
                createdAt = post.createdAt,
                status = PostStatus.STABLE,
            )
        )
    }
}
