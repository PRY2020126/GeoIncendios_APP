package com.example.geoincendios.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.geoincendios.R
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        webView.webChromeClient = object : WebChromeClient() {
        }
        webView.webViewClient = object : WebViewClient() {

        }

        val settings = webView.settings
        settings.javaScriptEnabled = true
        webView.loadUrl("https://sgonorte.bomberosperu.gob.pe/24horas")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}