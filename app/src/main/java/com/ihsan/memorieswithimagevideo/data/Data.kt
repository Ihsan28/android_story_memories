package com.ihsan.memorieswithimagevideo.data

import android.net.Uri
import androidx.lifecycle.MutableLiveData

class Data {

    init {
        contentUris.observeForever { mapContentUrisToMediaItems() }
    }

    companion object {
        val listOfAllowedVideoFormat = listOf("mp4", "mkv", "wmv")
        val listOfAllowedImageFormat = listOf("jpg", "jpeg", "png")

        //var contentUris = mutableListOf<Uri>()
        var contentUris = MutableLiveData(mutableListOf<Uri>())
        var mediaItems: MutableList<Pair<Uri, MediaType>> = mutableListOf()
        var currentIndex = 0

        var animationDuration = 3000L
        var coverRevealDuration = 1000L

        //Assigned in MainActivity
        var screenWidth = 0f
        var screenHeight = 0f
    }

    fun mapContentUrisToMediaItems() {
        mediaItems.clear()
        mediaItems.addAll(contentUris.value!!.map {
            val ext = it.toString().substring(it.toString().lastIndexOf(".") + 1)
            if (listOfAllowedVideoFormat.contains(ext)) {
                Pair(it, MediaType.VIDEO)
            } else {
                Pair(it, MediaType.IMAGE)
            }
        })
    }
}