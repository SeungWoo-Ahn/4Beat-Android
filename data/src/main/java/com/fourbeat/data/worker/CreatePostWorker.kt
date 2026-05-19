package com.fourbeat.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fourbeat.domain.model.post.CreatePostRequest
import com.fourbeat.domain.model.post.FileUploadUrlRequest
import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.model.post.VideoFileInfo
import com.fourbeat.domain.usecase.group.ConfirmPostUseCase
import com.fourbeat.domain.usecase.group.CreatePostUseCase
import com.fourbeat.domain.usecase.group.RollbackPostUseCase
import com.fourbeat.domain.usecase.post.GetFileUploadUrlUseCase
import com.fourbeat.domain.usecase.post.UploadVideoFileUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class CreatePostWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getFileUploadUrlUseCase: GetFileUploadUrlUseCase,
    private val uploadVideoFileUseCase: UploadVideoFileUseCase,
    private val createPostUseCase: CreatePostUseCase,
    private val confirmPostUseCase: ConfirmPostUseCase,
    private val rollbackPostUseCase: RollbackPostUseCase,
) : CoroutineWorker(context, workerParams) {
    /*
    * 게시글 작성 worker 실행
    * 최대 3회까지 재시도
    * 1. pre-signed url 발급
    * 2. 스토리지에 파일 직접 업로드
    * 3. 게시글 포스팅
    * */
    override suspend fun doWork(): Result {
        val tempId = inputData.getLong(KEY_TEMP_ID, 0L)
            .takeIf { it != 0L } ?: return Result.failure()

        val groupId = inputData.getLong(KEY_GROUP_ID, -1L)
        val songTitle = inputData.getString(KEY_SONG_TITLE)
            ?: run { rollbackPostUseCase(tempId); return Result.failure() }
        val songArtist = inputData.getString(KEY_SONG_ARTIST)
            ?: run { rollbackPostUseCase(tempId); return Result.failure() }
        val songImageUrl = inputData.getString(KEY_SONG_IMAGE_URL)
        val comment = inputData.getString(KEY_COMMENT)
        val filePath = inputData.getString(KEY_FILE_PATH)

        val videoFileInfo = filePath?.let {
            val file = File(it)
            if (!file.exists()) {
                rollbackPostUseCase(tempId)
                return Result.failure()
            }
            VideoFileInfo(file = file)
        }

        if (runAttemptCount >= MAX_RETRY_COUNT) {
            rollbackPostUseCase(tempId)
            return Result.failure()
        }

        val videoUrl = videoFileInfo?.let { (file) ->
            getFileUploadUrlUseCase(request = FileUploadUrlRequest(file.name))
                .getOrElse { return Result.retry() }
                .also {
                    uploadVideoFileUseCase(
                        uploadUrl = it.uploadUrl,
                        file = file,
                    ).getOrElse { return Result.retry() }
                }
                .videoUrl
        }

        val post = createPostUseCase(
            groupId = groupId,
            request = CreatePostRequest(
                song = Song(
                    title = songTitle,
                    artist = songArtist,
                    albumImageUrl = songImageUrl,
                ),
                comment = comment,
                videoUrl = videoUrl,
            ),
        ).getOrElse { return Result.retry() }

        confirmPostUseCase(tempId, post)

        return Result.success()
    }

    companion object {
        const val MAX_RETRY_COUNT = 3
        const val KEY_GROUP_ID = "key_group_id"
        const val KEY_SONG_TITLE = "key_song_title"
        const val KEY_SONG_ARTIST = "key_song_artist"
        const val KEY_SONG_IMAGE_URL = "key_song_image_url"
        const val KEY_COMMENT = "key_comment"
        const val KEY_FILE_PATH = "key_file_path"
        const val KEY_TEMP_ID = "key_temp_id"
    }
}
