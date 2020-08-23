package com.example.geoincendios.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Login : AppCompatActivity() {

    private val TAG = "Bryan"

    private lateinit var loginBtn: Button
    private lateinit var userService: UsuarioApiService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn = findViewById(R.id.login_button)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.62/geoincendios/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        userService = retrofit.create(UsuarioApiService::class.java)

        loginBtn.setOnClickListener {

            /*userService.getUsuarios().enqueue(object: Callback<List<Usuario>> {
                override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                    val usuarios = response!!.body()
                    Log.i(TAG, "hola")
                    Log.i(TAG, usuarios.toString())
                    Toast.makeText(this@Login,response.body().toString(),Toast.LENGTH_SHORT).show();
                }
                override fun onFailure(call: Call<List<Usuario>>?, t: Throwable?) {
                    t?.printStackTrace()
                }
            })
*/
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)

        }

        }


}

