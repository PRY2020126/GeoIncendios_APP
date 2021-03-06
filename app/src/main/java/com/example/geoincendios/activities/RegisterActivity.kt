package com.example.geoincendios.activities

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.models.Role
import com.example.geoincendios.models.Usuario
import com.example.geoincendios.util.URL_API
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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


        correoET.addTextChangedListener( object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if (!validarEmail(correoET.text.toString())) correoET.setError("Ingrese un correo válido")
                else correoET.setError(null)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        }
        )


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

            if(!validarEmail(correoET.text.toString()))
            {
                Toast.makeText(this,"Ingrese un correo valido" ,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            registrarbtn.isClickable = false
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
            val currentDate = sdf.format(Date())



            var usuario = Usuario(idusuario = "0", firtsName = nombresET.text.toString(),
                lastName = apellidosET.text.toString(), email = correoET.text.toString(),
                password = contrasenaET.text.toString(),role =  Role(3,"Móvil") , status = 1, user_reg = correoET.text.toString(),
                fec_reg = currentDate, cpc_reg = getAndroidId(), user_mod = null, cpc_mod = null, fec_mod = null )

            userService.saveUser(usuario).enqueue(object : Callback<UserDTO>{
                override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                    val usuario = response.body()

                    if (usuario!!.errorCode == "0" && usuario!!.data != null) {
                        Toast.makeText(this@RegisterActivity,"Registro Completo" ,Toast.LENGTH_LONG).show()
                        val i = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(i)
                        registrarbtn.isClickable = true
                        finish()

                    }
                    if (usuario!!.errorCode == "2"){
                        Toast.makeText(this@RegisterActivity,"El correo ya se encuentra registrado" ,Toast.LENGTH_LONG).show()
                        registrarbtn.isClickable = true
                        return
                    }
                }

                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity,"Registro Fallido" ,Toast.LENGTH_LONG).show()
                    registrarbtn.isClickable = true

                }
            })

        }
    }

    @Throws(IOException::class)
    fun getPublicIP(): String? {
        val doc = Jsoup.connect("http://www.checkip.org").get()
        return doc.getElementById("yourip").select("h1").first().select("span").text()
    }

    fun getAndroidId() : String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun validarEmail( email : String): Boolean{
        val pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }


}
