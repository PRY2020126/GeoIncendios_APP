package com.example.geoincendios.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geoincendios.R
import com.example.geoincendios.api.service.UserClient
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

class LoginActivity : AppCompatActivity() {

    private val TAG = "Bryan"

    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var userService: UsuarioApiService
    private lateinit var loginService: UserClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn = findViewById(R.id.login_button)
        registerBtn = findViewById(R.id.register_button)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.62:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        userService = retrofit.create(UsuarioApiService::class.java)
        loginService = retrofit.create(UserClient::class.java)

        var token = "eyJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE1OTgzMTQ3MzcsImlzcyI6Ind3dy5zZXJnaW9zZWtzLmNvbS5wZSIsInN1YiI6ImFkbWluIiwiZXhwIjoxNTk5MTc4NzM3fQ.Rl9v3865C6wiKsJZkJoxhCPNiqA0vMs4fs-u3dsg3n8z_SgNeWf8Z-GFqqtyPJfG6vvm-5PJtmGeYwvGKcys9w"

        registerBtn.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }

        loginBtn.setOnClickListener {


          /*  loginService.getSecret("Bearer "+token).enqueue(object : Callback<List<Usuario>>{
                override fun onResponse(call: Call<List<Usuario>>, response: Response<List<Usuario>>) {
                    val usuarios = response.body()
                    Log.i(TAG,response.body().toString())
                    Toast.makeText(this@LoginActivity, response.body().toString(),Toast.LENGTH_LONG).show()
                }

                override fun onFailure(call: Call<List<Usuario>>, t: Throwable) {
                    Log.i(TAG, "MAaaaal")
                }
            })*/

            loginService.getSecret3("Bearer "+token).enqueue(object : Callback<Any>{
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    val usuarios = response.body()
                    Log.i(TAG,response.body().toString())
                    Toast.makeText(this@LoginActivity, response.body().toString(),Toast.LENGTH_LONG).show()

                    val i = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(i)
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.i(TAG, "MAaaaal")
                }
            })

            /*val i = Intent(this, MainActivity::class.java)
            startActivity(i)
*/
        }

        }


}

