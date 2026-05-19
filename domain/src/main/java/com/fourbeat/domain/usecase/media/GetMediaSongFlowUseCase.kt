package com.fourbeat.domain.usecase.media

import com.fourbeat.domain.model.post.Song
import com.fourbeat.domain.repository.MediaRepository
import com.fourbeat.domain.usecase.music.ResolveBestMatchSongUseCase
import com.fourbeat.domain.usecase.music.SearchSongPageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetMediaSongFlowUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val searchSongPageUseCase: SearchSongPageUseCase,
    private val resolveBestMatchSongUseCase: ResolveBestMatchSongUseCase,
) {
    operator fun invoke(): Flow<Song?> =
        mediaRepository
            .getSongMetaFlow()
            .map { songMeta ->
                songMeta?.let {
                    Song(
                        title = it.title,
                        artist = it.artist,
                        albumImageUrl = it.albumImageUrl
                            ?: resolveBestAlbumImage(it.title, it.artist)
                    )
                }
            }

    private suspend fun resolveBestAlbumImage(title: String, artist: String): String? {
        val candidates = searchSongPageUseCase(query = title, limit = 10)
            .getOrNull()?.songs ?: return null
        return resolveBestMatchSongUseCase(
            targetTitle = title,
            targetArtist = artist,
            candidates = candidates
        )?.albumImageUrl ?: candidates.firstOrNull()?.albumImageUrl
    }
}