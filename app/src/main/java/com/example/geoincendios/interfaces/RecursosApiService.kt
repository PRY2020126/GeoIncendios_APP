package com.example.geoincendios.interfaces

import com.example.geoincendios.models.DTO.RecursosDTO
import com.example.geoincendios.models.Usuario
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface RecursosApiService {
    @GET("resources")
    fun getHidrantes(@Header("Authorization") authToken: String): Call<RecursosDTO>

    @GET("resources/districts/{id}")
    fun getHidrantesPorDistrito(@Header("Authorization") authToken: String, @Path("id") idDistrito:Long): Call<RecursosDTO>

}