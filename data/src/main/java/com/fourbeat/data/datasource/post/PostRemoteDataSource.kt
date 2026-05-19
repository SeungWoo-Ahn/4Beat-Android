package com.fourbeat.data.datasource.post

import com.fourbeat.data.network.di.ExternalStorage
import com.fourbeat.data.network.di.PrivateNetwork
import com.fourbeat.data.network.dto.post.FileUploadUrlRequestBody
import com.fourbeat.data.network.dto.post.FileUploadUrlResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRemoteDataSource @Inject constructor(
    @param:PrivateNetwork
    private val client: HttpClient,
    @param:ExternalStorage
    private val externalClient: HttpClient,
) : PostDataSource {
    override suspend fun getFileUploadUrl(body: FileUploadUrlRequestBody): FileUploadUrlResponse =
        client
            .post("posts/video-upload-url") {
                setBody(body)
            }.body()

    override suspend fun uploadVideoFile(uploadUrl: String, file: File) {
        externalClient.put(uploadUrl) {
            setBody(object : OutgoingContent.ReadChannelContent() {
                override val contentType = ContentType.Video.MP4
                override val contentLength = file.length()
                override fun readFrom(): ByteReadChannel = file.inputStream().toByteReadChannel()
            })
        }
    }
}
