package com.ihsan.memorieswithimagevideo.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.Utils.MyApplication
import com.ihsan.memorieswithimagevideo.data.MediaType

private const val TAG = "MiniPreviewAdapter"

class MiniPreviewAdapter(private val mediaItems: List<Pair<Uri, MediaType>>) :
    RecyclerView.Adapter<MiniPreviewAdapter.MiniPreviewViewHolder>() {

    private var onItemClick: ((Int) -> Unit)? = null

    inner class MiniPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniPreviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mini_preview_item, parent, false)
        return MiniPreviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: MiniPreviewViewHolder, position: Int) {
        val currentItem = mediaItems[position]
        Log.d(TAG, "onBindViewHolder: ${currentItem.first}")

        // Load and display thumbnails in the ImageView based on media type
        if (currentItem.second == MediaType.IMAGE) {
            // Load image thumbnail
            Glide.with(holder.thumbnailImageView)
                .asBitmap()
                .load(currentItem.first)
                .into(holder.thumbnailImageView)

            holder.thumbnailImageView.foreground = null

        } else if (currentItem.second == MediaType.VIDEO) {
            // Load video thumbnail
            Glide.with(holder.thumbnailImageView)
                .asBitmap()
                .load(currentItem.first)
                .into(holder.thumbnailImageView)
            holder.thumbnailImageView.foreground = AppCompatResources.getDrawable(
                MyApplication.instance,
                R.drawable.baseline_play_circle_outline_24)
        }
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClick = listener
    }
}

