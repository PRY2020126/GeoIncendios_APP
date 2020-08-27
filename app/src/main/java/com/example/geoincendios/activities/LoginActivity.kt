package com.example.geoincendios.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.text.set
import androidx.lifecycle.Transformations.map
import com.example.geoincendios.R
import com.example.geoincendios.fragments.MapsFragment
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.LoginDTO
import com.example.geoincendios.models.Usuario
import com.example.geoincendios.util.URL_API
import com.google.gson.Gson
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

    private lateinit var usernameET: EditText
    private lateinit var passwordET: EditText

    private lateinit var userService: UsuarioApiService

    private var converter = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn = findViewById(R.id.login_button)
        registerBtn = findViewById(R.id.register_button)

        usernameET = findViewById(R.id.et_login_email)
        passwordET = findViewById(R.id.et_login_password)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        userService = retrofit.create(UsuarioApiService::class.java)

        //pedirPermisos()

        usernameET.setText("testeo")

        var token = ""

        registerBtn.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }

        loginBtn.setOnClickListener {

            if(usernameET.text.isEmpty() || passwordET.text.isEmpty())
            {
                Toast.makeText(this@LoginActivity,"Por favor complete los campos" ,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            var user = LoginDTO(username = usernameET.text.toString(),password = passwordET.text.toString())

            Log.i("Usuario: ", user.toString())




            userService.generar_token(user).enqueue(object : Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if( response.headers().values("Authorization").isNullOrEmpty())
                    {
                        Toast.makeText(this@LoginActivity,"Usuario o contraseña no existe" ,Toast.LENGTH_LONG).show()
                        return
                    }
                    token = response.headers().values("Authorization")[0]

                    Log.i("Bryan",token)
                    //Toast.makeText(this@LoginActivity, token,Toast.LENGTH_LONG).show()


                    userService.login(token, user).enqueue(object : Callback<Any>{
                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            val usuarios = response.body()
                            Log.i(TAG,response.body().toString())
                            //Toast.makeText(this@LoginActivity, response.body().toString(),Toast.LENGTH_LONG).show()

                            Toast.makeText(this@LoginActivity, "Inicio de Sesión Correcto",Toast.LENGTH_LONG).show()


                            val i = Intent(this@LoginActivity, MainActivity::class.java)

                            startActivity(i)
                        }

                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            Log.i(TAG, "MAaaaal")
                        }
                    })



                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.i("MAAAAAAAAL", "MAaaaal")
                }
            })
        }

    }

    fun pedirPermisos(): Boolean {
        val requestCode = 101
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val per =
                arrayOf<String>(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            requestPermissions(per, requestCode)
            if (ActivityCompat.checkSelfPermission(this@LoginActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            else {
                return true
            }
        }
        return false
    }
}

