package com.ihsan.memorieswithimagevideo.Utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class StartEndDecoration(private val offset: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        // Add top margin only for the first item to avoid double space between items
        if (position == 0) {
            outRect.left = offset
        } else if (position == state.itemCount - 1) {
            outRect.right = offset
        }
    }
}
