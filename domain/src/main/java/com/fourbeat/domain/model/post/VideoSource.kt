package com.fourbeat.domain.model.post

import java.io.File

sealed interface VideoSource {
    data class Local(val file: File) : VideoSource
    data class Remote(val url: String) : VideoSource
}