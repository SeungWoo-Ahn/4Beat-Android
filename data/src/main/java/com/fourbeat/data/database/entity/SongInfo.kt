package com.fourbeat.data.database.entity

import androidx.room.ColumnInfo

data class SongInfo(
    @ColumnInfo(name = "songTitle") val title: String,
    @ColumnInfo(name = "songArtist") val artist: String,
    val albumImageUrl: String?,
)
