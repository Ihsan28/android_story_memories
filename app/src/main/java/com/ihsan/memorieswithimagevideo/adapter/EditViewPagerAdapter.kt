package com.ihsan.memorieswithimagevideo.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ihsan.memorieswithimagevideo.R

class EditViewPagerAdapter(private val contentUris: List<Uri>) :
    RecyclerView.Adapter<EditViewPagerAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val currentItemUri = contentUris[position]

        if (currentItemUri.toString().endsWith(".mp4")) {
            // It's a video
            holder.imageView.visibility = View.GONE
            holder.videoView.visibility = View.VISIBLE

            // Set up VideoView to play the video
            holder.videoView.setVideoURI(currentItemUri)
            holder.videoView.start()
        } else {
            // It's an image
            holder.videoView.visibility = View.GONE
            holder.imageView.visibility = View.VISIBLE

            // Load the image using Glide (you can use any image loading library)
            Glide.with(holder.imageView.context)
                .load(currentItemUri)
                .apply(RequestOptions().centerCrop())
                .into(holder.imageView)
        }
    }

    override fun getItemCount(): Int {
        return contentUris.size
    }
}
