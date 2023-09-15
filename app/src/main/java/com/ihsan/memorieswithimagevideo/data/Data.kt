package com.ihsan.memorieswithimagevideo.data

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class Data {
    companion object {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        var contentUris = mutableListOf<Uri>()
        var currentIndex = 0

        var animationDuration = 3000L
        var coverRevealDuration=500L

        //Assigned in MainActivity
        var screenWidth = 0f
        var screenHeight = 0f
    }
}