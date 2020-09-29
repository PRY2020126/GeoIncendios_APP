package com.example.geoincendios

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.geoincendios.interfaces.ZonaRiesgoApiService
import com.example.geoincendios.models.DTO.ZonaRiesgoDTO
import com.example.geoincendios.util.URL_API
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.String.format
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*


class MyService : Service() {

    private val TAG = "BackgroundService"

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder : Notification.Builder
    private val channelId = "com.example.geoincendios.activities"
    private val descrption = "Test notification"

    //private lateinit var

    private lateinit var zonaRiesgoApiService: ZonaRiesgoApiService

    private lateinit var zonaRiesgoList : MutableList<LatLng>

    private var notificado = false

    private var token = ""

    private var mHandler : Handler? = null

    var menor = false

    var mylocation : LatLng? = null

    var thread = Thread()

    var corriendo = false

    var sdf = SimpleDateFormat()

    private  var arrayOfHours : MutableList<String>? = null


    private lateinit var locationRequest  : LocationRequest
    private lateinit var locationCallback  : LocationCallback
    private lateinit var mFusedLocationClient  : FusedLocationProviderClient

    override fun onBind(p0: Intent?): IBinder? {
        Log.i(TAG, "OnBind()")
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate();
        Log.i(TAG,"Service Started, OnCreate()")
        val pref = getSharedPreferences("user", Context.MODE_PRIVATE)
        token =  pref.getString("token","")!!

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mFusedLocationClient = FusedLocationProviderClient(this!!)

        zonaRiesgoApiService = retrofit.create(ZonaRiesgoApiService::class.java)
        zonaRiesgoList = arrayListOf()

        mHandler = Handler()
        obtenerZonas()

        arrayOfHours = arrayListOf("12:00","06:00", "18:00")
        sdf = SimpleDateFormat("HH:mm")

        Log.i("Hora ", arrayOfHours.toString())

        traerUbicacion()
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(CHANNEL_ID, "de zonas de riesgo", NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Se esta ejecutando GeoIncendios en Sengundo Plano")
                .setContentText("Se le notificara si se encuentra cerca de una zona de riesgo").build()
            startForeground(1, notification)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service Started, OnStart()")
        var runnable = Runnable {
            Log.i(TAG,"Entro")
                while (corriendo){
                            Log.i("Corriendo",corriendo.toString())
                            menor = false
                             Log.i("Hora",sdf.format(Date()))
                            Log.i("Ubicacion",mylocation.toString() )
                            if(arrayOfHours!!.contains(sdf.format(Date())))
                            {
                                obtenerZonas()
                            }

                            Thread.sleep(20000)

                            if (!notificado && menor)
                            {
                                createNoti()
                                notificado = true
                            }
                            if (notificado && !menor)
                            {
                                notificationManager.cancel(1234)
                                notificado = false
                                menor = false
                            }

                            Log.i(TAG,"Durmiendo Hilo")
             }
        }
        corriendo = true
        thread = Thread(runnable)
        thread.start()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun traerUbicacion(){

        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(20 * 1000)
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    return;
                }
                for (location in locationResult.getLocations()) {
                    if (location != null) {
                        mylocation = LatLng(location.latitude, location.longitude)
                    }
                }
                super.onLocationResult(locationResult)
            }
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        sendBroadcast(Intent("IWillStartAuto"))

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "OnDestory")
        corriendo = false
        sendBroadcast(Intent("IWillStartAuto"))
    }

    override fun onLowMemory() {
        Log.i(TAG, "OnLowMemory")
    }

    private fun createNoti(){
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, Splash_screen::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel = NotificationChannel(channelId,"Canal 1",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)

            val bitmap = BitmapFactory.decodeResource(resources,R.drawable.logo)

            builder = Notification.Builder(this,channelId)
                .setContentTitle("Alerta")
                .setContentText("Se encuentra dentro de una zona de riesgo")
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        }
        else {
            val bitmap = BitmapFactory.decodeResource(resources,R.drawable.logo)

            builder = Notification.Builder(this)
                .setContentTitle("Alerta")
                .setContentText("Se encuentra dentro de una zona de riesgo")
                .setSmallIcon(R.drawable.logo)
                .setColor(Color.RED)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234,builder.build())

    }

    private fun hallaDistancia(lat1: Double,lat2: Double,lng1: Double,lng2 : Double): Double{
        val R = 6371;
        val dLat = deg2rad(lat2-lat1)
        val dLng = deg2rad(lng2-lng1)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                Math.sin(dLng/2) * Math.sin(dLng/2)
        ;
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        val d = R * c; // Distance in km
        return d;
    }

    private fun deg2rad(deg: Double): Double{
        return deg * (Math .PI/180)
    }

    @SuppressLint("MissingPermission")
    private fun obtenerZonas()
    {
        zonaRiesgoList.clear()
        zonaRiesgoApiService.getZonasRiesgo(token!!).enqueue(object : Callback<ZonaRiesgoDTO> {
            override fun onResponse(call: Call<ZonaRiesgoDTO>, response: Response<ZonaRiesgoDTO>) {
                val zonas = response.body()
                for (item in zonas!!.data)
                {
                    zonaRiesgoList.add(LatLng(item.latitude!!,item.longitude!!))
                }

                for (item in zonaRiesgoList)
                {
                    val dist = hallaDistancia(mylocation!!.latitude,item.latitude,mylocation!!.longitude,item.longitude)
                    if (dist < 0.5)
                    {
                        menor = true
                    }
                    Log.i("Distancias", dist.toString())
                }
                Log.i("Menor", menor.toString())

            }
            override fun onFailure(call: Call<ZonaRiesgoDTO>, t: Throwable) {
                Log.i("AAAA", "MAaaaal")
            }
        })
    }


}