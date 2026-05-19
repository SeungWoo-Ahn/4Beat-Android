package com.fourbeat.data.datasource.post

import com.fourbeat.data.network.dto.post.FileUploadUrlRequestBody
import com.fourbeat.data.network.dto.post.FileUploadUrlResponse
import java.io.File

interface PostDataSource {
    suspend fun getFileUploadUrl(body: FileUploadUrlRequestBody): FileUploadUrlResponse
    suspend fun uploadVideoFile(uploadUrl: String, file: File)
}
