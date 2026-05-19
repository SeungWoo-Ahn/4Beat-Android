package com.fourbeat.domain.repository

import com.fourbeat.domain.model.post.SongPage

interface MusicRepository {
    suspend fun searchSongPage(query: String, limit: Int, nextUrl: String?): SongPage
}
