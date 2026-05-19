package com.fourbeat.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.fourbeat.data.worker.CreatePostWorker
import com.fourbeat.data.worker.CreatePostWorker.Companion.KEY_COMMENT
import com.fourbeat.data.worker.CreatePostWorker.Companion.KEY_FILE_PATH
import com.fourbeat.data.worker.CreatePostWorker.Companion.KEY_GROUP_ID
import com.fourbeat.data.worker.CreatePostWorker.Companion.KEY_SONG_ARTIST
import com.fourbeat.data.worker.CreatePostWorker.Companion.KEY_SONG_IMAGE_URL
import com.fourbeat.data.worker.CreatePostWorker.Companion.KEY_SONG_TITLE
import com.fourbeat.data.worker.CreatePostWorker.Companion.KEY_TEMP_ID
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import java.util.concurrent.TimeUnit
import com.fourbeat.domain.model.post.CreatePostRequest
import com.fourbeat.domain.model.post.VideoFileInfo
import com.fourbeat.domain.repository.WorkRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkRepositoryImpl @Inject constructor(
    private val workManager: WorkManager,
) : WorkRepository {
    /*
    * 게시글 작성 Worker 등록
    * 네트워크 연결 시에 시도, 재시도 시 15초의 딜레이 추가
    * */
    override fun enqueueCreatePost(
        groupId: Long,
        tempId: Long,
        request: CreatePostRequest,
        videoFileInfo: VideoFileInfo?,
    ) {
        val inputData = workDataOf(
            KEY_GROUP_ID to groupId,
            KEY_TEMP_ID to tempId,
            KEY_SONG_TITLE to request.song.title,
            KEY_SONG_ARTIST to request.song.artist,
            KEY_SONG_IMAGE_URL to request.song.albumImageUrl,
            KEY_COMMENT to request.comment,
            KEY_FILE_PATH to videoFileInfo?.file?.absolutePath,
        )

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<CreatePostWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniqueWork(
            "create_post_$groupId",
            ExistingWorkPolicy.KEEP,
            workRequest,
        )
    }
}
