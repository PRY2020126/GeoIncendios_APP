package com.example.geoincendios.util

import android.content.Context
import android.os.AsyncTask
import android.provider.Settings.Secure
import org.jsoup.Jsoup
import java.security.AccessController.getContext

class DeviceId(context :Context){
    private val android_id = Secure.getString(
        context.contentResolver,
        Secure.ANDROID_ID)
}

class RetrieveFeedTask : AsyncTask<Void, Void, String>() {
    private var exception: Exception? = null

    protected override fun doInBackground(vararg p0: Void?): String? {
        return try {
            val doc = Jsoup.connect("http://www.checkip.org").get()
            return doc.getElementById("yourip").select("h1").first().select("span").text()
        } catch (e: Exception) {
            exception = e
            return null
        } finally {
        }
    }
}