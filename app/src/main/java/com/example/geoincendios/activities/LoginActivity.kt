package com.example.geoincendios.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emmanuelkehinde.shutdown.Shutdown
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.LoginDTO
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.util.URL_API
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class LoginActivity : AppCompatActivity() {

    private val INTERVALO = 2000
    //private lateinit var tiempoPrimerClick : Long

    private val TAG = "Bryan"
    private val key = "Correo"

    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button

    private lateinit var correoET: EditText
    private lateinit var passwordET: EditText

    private lateinit var userService: UsuarioApiService

    private var converter = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //PreferenceManager
        val prefs = getSharedPreferences("user", Context.MODE_PRIVATE)
        prefs.getString("email","")
        prefs.getString("password","")
        val editor = prefs.edit()

        loginBtn = findViewById(R.id.login_button)
        registerBtn = findViewById(R.id.register_button)

        correoET = findViewById(R.id.et_login_email)
        passwordET = findViewById(R.id.et_login_password)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        userService = retrofit.create(UsuarioApiService::class.java)

        //pedirPermisos()

        correoET.setText(prefs.getString("email",""))

        passwordET.setText(prefs.getString("password",""))

        var token = ""

        registerBtn.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }

        loginBtn.setOnClickListener {

            if(correoET.text.isEmpty() || passwordET.text.isEmpty())
            {
                Toast.makeText(this@LoginActivity,"Por favor, complete los campos" ,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var user = LoginDTO(email = correoET.text.toString(),password = passwordET.text.toString())

            userService.generar_token(user).enqueue(object : Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if( response.headers().values("Authorization").isNullOrEmpty())
                    {
                        Toast.makeText(this@LoginActivity,"Correo o contraseña no existe" ,Toast.LENGTH_LONG).show()
                        return
                    }
                    token = response.headers().values("Authorization")[0]

                    Log.i("Bryan",token)

                    editor.putString("token",token)

                    userService.login(token, user).enqueue(object : Callback<UserDTO>{
                        override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                            val usuario = response.body()
                            Log.i(TAG,response.body().toString())
                            //Toast.makeText(this@LoginActivity, usuario!!.data.toString(),Toast.LENGTH_LONG).show()

                            Toast.makeText(this@LoginActivity, "Inicio de Sesión Correcto",Toast.LENGTH_LONG).show()


                            editor.putString("idusuario",usuario!!.data.idusuario.toString())
                            editor.putString("email",correoET.text.toString())
                            editor.putString("password",passwordET.text.toString())
                            editor.putString("name",usuario!!.data.firtsName + usuario!!.data.lastName)
                            editor.putString("role",usuario!!.data.role.role)
                            editor.putString("status", usuario!!.data.status.toString())
                            editor.apply()



                            val i = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(i)
                            finish()
                        }

                        override fun onFailure(call: Call<UserDTO>, t: Throwable) {
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

    override fun onBackPressed() {
        Shutdown.now(this)
    }

}

