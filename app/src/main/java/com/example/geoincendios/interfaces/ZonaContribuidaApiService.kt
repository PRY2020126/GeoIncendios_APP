package com.example.geoincendios.interfaces

import com.example.geoincendios.models.Usuario
import com.example.geoincendios.models.ZonaContribuida
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ZonaContribuidaApiService {
    @POST("contribuited-zones")
    fun saveZonaContribuida(@Header("Authorization") authToken: String,
                 @Body zonaContribuida: ZonaContribuida): Call<Any>
}