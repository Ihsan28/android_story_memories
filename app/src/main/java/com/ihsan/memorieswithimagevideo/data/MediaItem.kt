package com.ihsan.memorieswithimagevideo.data

import android.net.Uri

data class MediaItem(
    val mediaType: MediaType,
    val mediaUri: Uri,
    val thumbnailUri: Uri
)

enum class MediaType {
    IMAGE, VIDEO
}