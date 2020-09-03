package com.example.geoincendios.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.ResponseDTO
import com.example.geoincendios.models.Role
import com.example.geoincendios.models.Usuario
import com.example.geoincendios.util.URL_API
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {


    private lateinit var registrarbtn: Button

    private lateinit var nombresET: EditText
    private lateinit var apellidosET: EditText
    private lateinit var correoET: EditText
    private lateinit var contrasenaET: EditText
    private lateinit var repetir_contrasenaET: EditText

    private lateinit var userService: UsuarioApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        userService = retrofit.create(UsuarioApiService::class.java)

        registrarbtn = findViewById(R.id.registrar_button)

        nombresET = findViewById(R.id.et_register_nombres)
        apellidosET = findViewById(R.id.et_register_apellidos)
        correoET = findViewById(R.id.et_register_correo)
        contrasenaET = findViewById(R.id.et_register_contrasena)
        repetir_contrasenaET = findViewById(R.id.et_register_contrasena_repeat)




        registrarbtn.setOnClickListener {


            if(nombresET.text.isEmpty() || apellidosET.text.isEmpty() || correoET.text.isEmpty()
                 || contrasenaET.text.isEmpty() || repetir_contrasenaET.text.isEmpty())
            {
                Toast.makeText(this,"Complete los campos" ,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if( contrasenaET.text.toString() != repetir_contrasenaET.text.toString())
            {
                Toast.makeText(this,"Las contraseñas no coinciden" ,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }




            var usuario = Usuario(idusuario = "0", firtsName = nombresET.text.toString(),
                lastName = apellidosET.text.toString(), email = correoET.text.toString(),
                password = contrasenaET.text.toString(),role =  Role(3,"Móvil") , status = 1, user_reg = null,
                fec_reg = "2020-08-14T02:14:32.000+00:00", cpc_reg = null, user_mod = null, cpc_mod = null, fec_mod = null )

            //Toast.makeText(this@RegisterActivity,usuario.toString() ,Toast.LENGTH_LONG).show()


            userService.saveUser(usuario).enqueue(object : Callback<Any>{
                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    val usuarios = response.body()
                    Log.i("Bryan",response.body().toString())
                    //Toast.makeText(this@RegisterActivity, response.body().toString(),Toast.LENGTH_LONG).show()

                    Toast.makeText(this@RegisterActivity,"Registro Completo" ,Toast.LENGTH_LONG).show()

                    val i = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(i)
                    finish()
                }

                override fun onFailure(call: Call<Any>, t: Throwable) {
                    Log.i("MAAAAAAAAL", "MAaaaal")
                    Toast.makeText(this@RegisterActivity,"Registro Fallido" ,Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}