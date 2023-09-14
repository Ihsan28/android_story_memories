package com.ihsan.memorieswithimagevideo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ihsan.memorieswithimagevideo.data.Data.Companion.screenHeight
import com.ihsan.memorieswithimagevideo.data.Data.Companion.screenWidth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        screenWidth = resources.displayMetrics.widthPixels.toFloat()
        screenHeight = resources.displayMetrics.heightPixels.toFloat()
    }
}
