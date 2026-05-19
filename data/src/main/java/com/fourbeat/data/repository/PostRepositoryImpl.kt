package com.fourbeat.data.repository

import com.fourbeat.data.datasource.post.PostDataSource
import com.fourbeat.data.mapper.asBody
import com.fourbeat.data.mapper.toDomain
import com.fourbeat.domain.model.post.FileUploadUrl
import com.fourbeat.domain.model.post.FileUploadUrlRequest
import com.fourbeat.domain.repository.PostRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDataSource: PostDataSource,
) : PostRepository {
    override suspend fun getFileUploadUrl(request: FileUploadUrlRequest): FileUploadUrl =
        postDataSource.getFileUploadUrl(request.asBody()).toDomain()

    override suspend fun uploadVideoFile(uploadUrl: String, file: File) =
        postDataSource.uploadVideoFile(uploadUrl, file)
}
