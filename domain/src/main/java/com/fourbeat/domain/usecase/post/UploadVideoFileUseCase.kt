package com.fourbeat.domain.usecase.post

import com.fourbeat.domain.exception.PostException
import com.fourbeat.domain.repository.PostRepository
import java.io.File
import javax.inject.Inject

class UploadVideoFileUseCase @Inject constructor(
    private val postRepository: PostRepository,
) {
    suspend operator fun invoke(uploadUrl: String, file: File): Result<Unit> =
        runCatching {
            postRepository.uploadVideoFile(uploadUrl, file)
        }.recoverCatching {
            throw PostException.VideoUploadFailed(it)
        }
}
