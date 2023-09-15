package com.ihsan.memorieswithimagevideo.adapter

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ihsan.memorieswithimagevideo.R
import com.ihsan.memorieswithimagevideo.data.MediaItem
import com.ihsan.memorieswithimagevideo.data.MediaType
import java.io.File
import java.io.FileOutputStream

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

        // Load and display thumbnails in the ImageView based on media type
        if (currentItem.second == MediaType.IMAGE) {
            // Load image thumbnail
            Glide.with(holder.thumbnailImageView)
                .asBitmap()
                .load(currentItem.first)
                .into(holder.thumbnailImageView)
        } else if (currentItem.second == MediaType.VIDEO) {
            // Load video thumbnail
            Glide.with(holder.thumbnailImageView)
                .asGif()
                .load(currentItem.first)
                .into(holder.thumbnailImageView)
        }
    }

    /*fun extractVideoThumbnailUri(videoUri: Uri, context: Context): Uri? {
        val retriever = MediaMetadataRetriever()

        try {
            retriever.setDataSource(context, videoUri)
            val frame = retriever.getFrameAtTime() // Get the first frame (thumbnail)
            val thumbnailUri = frame?.let { saveBitmapAsImageFile(context, it) }

            return thumbnailUri
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }

        return null
    }

    private fun saveBitmapAsImageFile(context: Context, bitmap: Bitmap): Uri? {
        val fileName = "thumbnail_${System.currentTimeMillis()}.jpg"
        val outputStream: FileOutputStream
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        val imagePath = File(context.filesDir, fileName).absolutePath
        return Uri.parse("file://$imagePath")
    }*/

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClick = listener
    }
}

