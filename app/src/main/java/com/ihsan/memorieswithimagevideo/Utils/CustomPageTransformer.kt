package com.ihsan.memorieswithimagevideo.Utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import java.lang.Float.max
import java.lang.Math.abs

class CustomPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        // The 'position' parameter indicates the position of the current page
        // relative to its position in the center of the ViewPager.

        // Example: Fade-out animation for pages to the left
        if (position < -1) {
            // Page is off-screen to the left
            page.alpha = 0f
        } else if (position <= 1) {
            // Page is within the screen range (-1 to 1)
            // You can apply custom animations here based on the position.

            // Example: Fade-in animation for pages in the center
            page.alpha = 1 - abs(position)

            // Example: Scale-down animation for pages
            val scaleFactor = max(0.75f, 1 - abs(position) / 4)
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor

            // Example: Translation animation for pages
            val translationX = page.width * -position
            page.translationX = translationX

        } else {
            // Page is off-screen to the right
            page.alpha = 0f
        }
    }
}