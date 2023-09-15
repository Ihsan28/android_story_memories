package com.ihsan.memorieswithimagevideo.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ihsan.memorieswithimagevideo.R

class MiniPreviewAdapter(private val contentUris: List<Uri>) :
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
        // Load and display thumbnails in the ImageView
        val currentItemUri = contentUris[position]

        // Load thumbnails using Glide or another image loading library
        // You can create thumbnail versions of your images/videos to improve performance

        Glide.with(holder.thumbnailImageView)
            .load(currentItemUri)
            .into(holder.thumbnailImageView)
    }

    override fun getItemCount(): Int {
        return contentUris.size
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClick = listener
    }
}
