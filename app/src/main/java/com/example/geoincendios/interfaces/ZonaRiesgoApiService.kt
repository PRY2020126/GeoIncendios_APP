package com.example.geoincendios.interfaces

import com.example.geoincendios.models.DTO.LoginDTO
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.models.DTO.ZonaRiesgoDTO
import com.example.geoincendios.models.Usuario
import com.example.geoincendios.models.ZonaRiesgo
import retrofit2.Call
import retrofit2.http.*

interface ZonaRiesgoApiService {

    @GET("zones")
    fun getZonasRiesgo(@Header("Authorization") authToken: String): Call<ZonaRiesgoDTO>

}