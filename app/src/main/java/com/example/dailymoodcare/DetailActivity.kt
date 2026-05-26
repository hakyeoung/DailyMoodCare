package com.example.dailymoodcare

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        webView = findViewById(R.id.webview_youtube)
        tvTitle = findViewById(R.id.tv_detail_title)
        tvDescription = findViewById(R.id.tv_detail_description)

        // VideoRecommendActivity에서 전달된 데이터 받기
        val videoId = intent.getStringExtra("VIDEO_ID") ?: ""
        val title = intent.getStringExtra("VIDEO_TITLE") ?: "제목 없음"
        val desc = intent.getStringExtra("VIDEO_DESC") ?: "설명 없음"

        tvTitle.text = title
        tvDescription.text = desc

        // WebView 설정하여 유튜브 영상 재생
        if (videoId.isNotEmpty()) {
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
            }
            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = android.webkit.WebViewClient()
            val embedHtml = """
                <html>
                <body style="margin:0;padding:0;">
                <iframe width="100%" height="100%" 
                        src="https://www.youtube.com/embed/$videoId?autoplay=1" 
                        frameborder="0" allow="autoplay; encrypted-media" allowfullscreen>
                </iframe>
                </body>
                </html>
            """.trimIndent()
            webView.loadData(embedHtml, "text/html", "utf-8")
        }
    }
}
