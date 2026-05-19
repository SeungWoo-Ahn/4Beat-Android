package com.fourbeat.domain.repository

import com.fourbeat.domain.model.post.FileUploadUrl
import com.fourbeat.domain.model.post.FileUploadUrlRequest
import java.io.File

interface PostRepository {
    suspend fun getFileUploadUrl(request: FileUploadUrlRequest): FileUploadUrl
    suspend fun uploadVideoFile(uploadUrl: String, file: File)
}
