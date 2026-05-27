package com.example.dailymoodcare

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
                mediaPlaybackRequiresUserGesture = true
                loadsImagesAutomatically = true
            }
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = WebViewClient()

            val embedHtml = """
                <!doctype html>
                <html lang="ko">
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <meta name="referrer" content="strict-origin-when-cross-origin">
                    <style>
                        html, body { margin: 0; padding: 0; width: 100%; height: 100%; background: #000; }
                        iframe { width: 100%; height: 100%; border: 0; display: block; }
                    </style>
                </head>
                <body>
                    <iframe
                        src="https://www.youtube-nocookie.com/embed/$videoId?autoplay=0&playsinline=1&rel=0&origin=https://www.youtube-nocookie.com"
                        referrerpolicy="strict-origin-when-cross-origin"
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                        allowfullscreen>
                    </iframe>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(
                "https://www.youtube-nocookie.com/",
                embedHtml,
                "text/html",
                "utf-8",
                null
            )
        }
    }
}
