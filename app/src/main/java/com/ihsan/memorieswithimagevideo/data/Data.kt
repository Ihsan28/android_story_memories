package com.ihsan.memorieswithimagevideo.data

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.ihsan.memorieswithimagevideo.Utils.MyApplication

private const val TAG = "Data"

class Data {

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
            } else if (listOfAllowedImageFormat.contains(ext)) {
                Pair(it, MediaType.IMAGE)
            } else {
                val mimeType = MyApplication.instance.contentResolver.getType(it)
                if (mimeType != null) {
                    when {
                        mimeType.startsWith("image") -> Pair(it, MediaType.IMAGE)
                        mimeType.startsWith("video") -> Pair(it, MediaType.VIDEO)
                        else -> {
                            Toast.makeText(
                                MyApplication.instance,
                                "File format not supported: $mimeType",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(
                                TAG,
                                "mapContentUrisToMediaItems: File format not supported: $mimeType"
                            )
                            Pair(it, MediaType.IMAGE) // Or choose a default type
                        }
                    }
                } else {
                    Toast.makeText(
                        MyApplication.instance,
                        "File format not supported: $ext",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d(
                        TAG,
                        "mapContentUrisToMediaItems: File format not supported: $ext"
                    )

                    Pair(it, MediaType.IMAGE)
                }
            }
        })
        mediaItems.map { Log.d(TAG, "mapContentUrisToMediaItems: $it") }
    }
}