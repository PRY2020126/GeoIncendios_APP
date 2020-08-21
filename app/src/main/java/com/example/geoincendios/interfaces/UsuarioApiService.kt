package com.example.geoincendios.interfaces

import com.example.geoincendios.models.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UsuarioApiService {

    @POST("insertar_producto.php")
    fun getAllUsers(@Body loginRequest: Usuario): Call<Usuario>


    @GET("recibir_usuarios.php")
    fun getUsuarios(): Call<List<Usuario>>

    @GET("insertar_producto.php")
    fun getUsuarioById(@Path("id") id: Long): Call<Usuario>

}