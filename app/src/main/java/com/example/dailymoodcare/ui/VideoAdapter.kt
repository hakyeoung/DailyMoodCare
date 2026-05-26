package com.example.dailymoodcare.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dailymoodcare.R
import com.example.dailymoodcare.data.VideoItem

class VideoAdapter(
    private val videoList: List<VideoItem>,
    private val onItemClick: (VideoItem) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_video_title)
        val tvDescription: TextView = itemView.findViewById(R.id.tv_video_description)
        val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_video_thumbnail)

        fun bind(videoItem: VideoItem) {
            tvTitle.text = videoItem.title
            tvDescription.text = videoItem.description
            
            // Glide 라이브러리를 통해 썸네일 이미지 로드
            com.bumptech.glide.Glide.with(itemView.context)
                .load(videoItem.thumbnailUrl)
                .into(ivThumbnail)
            
            itemView.setOnClickListener {
                onItemClick(videoItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videoList[position])
    }

    override fun getItemCount(): Int = videoList.size
}
