package com.example.geoincendios.interfaces

import com.example.geoincendios.models.DTO.LoginDTO
import com.example.geoincendios.models.DTO.ResponseDTO
import com.example.geoincendios.models.Usuario
import retrofit2.Call
import retrofit2.http.*

interface UsuarioApiService {

    @POST("users")
    fun saveUser(@Body usuario: Usuario): Call<Any>

    @GET("users")
    fun getUsers(@Header("Authorization") authToken: String): Call<Usuario>


    @GET("users")
    fun getJson(@Header("Authorization") authToken: String): Call<Any>

    @POST("login")
    fun generar_token(@Body user: LoginDTO ): Call<Void>

    @POST("users/login")
    fun login(@Header("Authorization") authToken: String,@Body user: LoginDTO ): Call<Any>



}