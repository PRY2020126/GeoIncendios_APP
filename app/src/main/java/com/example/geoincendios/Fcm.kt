package com.example.geoincendios

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import com.example.geoincendios.activities.MainActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.squareup.picasso.Picasso
import java.io.IOException
import kotlin.random.Random

class Fcm : FirebaseMessagingService() {
    override fun onNewToken(s: String?) {
        if (s != null)
        {
            guardarToken(s)
        }
    }

    private fun guardarToken(s: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("token")
        ref.child("GeoIncendios").setValue(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        if(remoteMessage!!.data.isNotEmpty()){
            val titulo = remoteMessage.data["titulo"]
            val detalle = remoteMessage.data["detalle"]
            val foto = remoteMessage.data["foto"]

            notificicacionOreo(titulo, detalle,foto)


        }



    }

    private fun notificicacionOreo(titulo:String?, detalle:String?, foto: String?) {
        val id = "mensaje"

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this,id)
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            val nc = NotificationChannel(id,"nuevo", NotificationManager.IMPORTANCE_HIGH)
            nc.setShowBadge(true)
            nm.createNotificationChannel(nc)
        }

        try {
            val img_foto = Picasso.get().load(foto) as Bitmap
            builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(titulo)
                .setContentText(detalle)
                .setContentIntent(clickNoti())
                .setSmallIcon(R.drawable.logo)
                .setStyle(NotificationCompat.BigPictureStyle().bigPicture(img_foto).bigLargeIcon(null))
            val random = Random
            val notirandom = random.nextInt(8000)
            nm.notify(notirandom, builder.build())
        } catch (e:IOException){
            e.printStackTrace()
        }
    }

    fun clickNoti():PendingIntent{
        val nf = Intent(applicationContext, MainActivity::class.java)
        nf.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(this, 0, nf, 0)
    }

}