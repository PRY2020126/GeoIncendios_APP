package com.example.geoincendios

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geoincendios.activities.LoginActivity
import com.example.geoincendios.activities.MainActivity
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.LoginDTO
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.util.URL_API
import kotlinx.android.synthetic.main.splash_screen.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Splash_screen : AppCompatActivity() {

    private lateinit var locatioManager: LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)


        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var userService = retrofit.create(UsuarioApiService::class.java)

        val prefs = getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        var email = prefs.getString("email","")
        var pass = prefs.getString("password","")
        var token = prefs.getString("token","")


        iv_note.alpha =0f

        fun Ingresar(){

            val user = LoginDTO(email = email!!,password = pass!!)
            userService.login(token!!, user).enqueue(object : Callback<UserDTO> {
                override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                    val usuario = response.body()

                    if (usuario?.data == null){
                        editor.clear()
                        editor.commit()
                        starLogin()
                        return
                    }

                    Toast.makeText(this@Splash_screen, "Inicio de Sesión Correcto", Toast.LENGTH_LONG).show()
                    val i = Intent(this@Splash_screen, MainActivity::class.java)
                    startActivity(i)
                    finish()
                }
                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    Toast.makeText(this@Splash_screen, "Ha ocurrido un error en el servidor", Toast.LENGTH_LONG).show()
                    editor.clear()
                    editor.commit()
                    starLogin()
                    return
                }
            })

        }


        iv_note.animate().setDuration(700).alpha(1f).withEndAction(){

            if(email != "" && pass != "" && token!= "")
            {
                Ingresar()
            }
            else
            {
                starLogin()
            }
        }
    }

    private fun starLogin(){
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

}
