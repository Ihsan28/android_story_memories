package com.ihsan.memorieswithimagevideo.data

import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import com.ihsan.memorieswithimagevideo.Utils.MyApplication
import java.io.File

private const val TAG = "Data"

class Data {

    companion object {
        val listOfAllowedVideoFormat = listOf("mp4", "mkv", "wmv")
        val listOfAllowedImageFormat = listOf("jpg", "jpeg", "png")

        //var contentUris = mutableListOf<Uri>()
        var contentUris = MutableLiveData(mutableListOf<Uri>())
        var mediaItems = mutableListOf<Pair<Uri, MediaType>>()
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
                        mimeType.startsWith("image") -> Pair(getMediaFileFromContentUri(it,MediaType.IMAGE)!!, MediaType.IMAGE)
                        mimeType.startsWith("video") -> Pair(getMediaFileFromContentUri(it,MediaType.VIDEO)!!, MediaType.VIDEO)
                        else -> {
                            Log.e(
                                TAG,
                                "mapContentUrisToMediaItems: File format not supported: $mimeType"
                            )
                            Pair(it, MediaType.IMAGE) // Or choose a default type
                        }
                    }
                } else {
                    Log.e(
                        TAG,
                        "mapContentUrisToMediaItems: File format not supported: $ext"
                    )

                    Pair(it, MediaType.IMAGE)
                }
            }
        })
        mediaItems.map { Log.d(TAG, "mapContentUrisToMediaItems: $it") }
    }

    private fun getMediaFileFromContentUri(contentUri: Uri, mediaType:MediaType): Uri? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = MyApplication.instance.contentResolver.query(contentUri, projection, null, null, null)

        cursor?.use {cursor1->

            val columnIndex =if (mediaType == MediaType.VIDEO) {
                cursor1.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            } else {
                cursor1.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            }

            if (cursor1.moveToFirst()) {
                val mediaPath = cursor1.getString(columnIndex)
                return if (!mediaPath.isNullOrEmpty()) {
                    File(mediaPath).toUri()
                } else {
                    null
                }
            }
        }

        return null
    }
}