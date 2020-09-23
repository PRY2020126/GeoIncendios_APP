package com.example.geoincendios.interfaces

import com.example.geoincendios.models.DTO.LoginDTO
import com.example.geoincendios.models.DTO.ResponseDTO
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.models.Email
import com.example.geoincendios.models.Usuario
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.*

interface FirebaseService {

    @POST(".")
    fun llamarEspecifico(@Body notificacion: JSONObject, @Header("Authorization") authToken: String): Call<Any>






}