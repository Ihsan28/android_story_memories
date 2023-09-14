package com.ihsan.memorieswithimagevideo.data

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class Data {
    companion object {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        var imageUris = mutableListOf<Uri>()
        var currentImageIndex = 0

        const val animationDuration = 3000L
    }
}