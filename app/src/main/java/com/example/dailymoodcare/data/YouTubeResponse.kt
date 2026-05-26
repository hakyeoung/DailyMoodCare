package com.example.dailymoodcare.data

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem>
)

data class YouTubeSearchItem(
    val id: VideoId,
    val snippet: VideoSnippet
)

data class VideoId(
    val videoId: String?
)

data class VideoSnippet(
    val title: String,
    val description: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val default: ThumbnailDetail,
    val medium: ThumbnailDetail,
    val high: ThumbnailDetail
)

data class ThumbnailDetail(
    val url: String
)
