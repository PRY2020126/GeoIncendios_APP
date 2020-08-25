package com.example.geoincendios.api.service
import com.example.geoincendios.api.model.Login
import com.example.geoincendios.models.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserClient {

    @POST("login")
    fun GetAllUsers(@Body login:Login): Call<List<Usuario>>


    @GET("users")
    fun getSecret(@Header("Authorization") authToken: String): Call<List<Usuario>>

    @GET("users")
    fun getSecret2(@Header("Authorization") authToken: String): Call<Usuario>

    @GET("users")
    fun getSecret3(@Header("Authorization") authToken: String): Call<Any>
}