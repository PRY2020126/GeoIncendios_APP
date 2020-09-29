package com.example.geoincendios.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emmanuelkehinde.shutdown.Shutdown
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.LoginDTO
import com.example.geoincendios.models.DTO.ResponseDTO
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.models.Email
import com.example.geoincendios.util.URL_API
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class LoginActivity : AppCompatActivity() {



    private val TAG = "Bryan"
    private val key = "Correo"

    private lateinit var loginBtn: Button
    private lateinit var registerBtn: Button
    private lateinit var recuperar_contrasena : TextView

    private lateinit var correoET: EditText
    private lateinit var passwordET: EditText

    private lateinit var userService: UsuarioApiService

    private lateinit var locatioManager: LocationManager


    private lateinit var  btn_enviar_contra : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //PreferenceManager
        val prefs = getSharedPreferences("user", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        loginBtn = findViewById(R.id.login_button)
        registerBtn = findViewById(R.id.register_button)
        recuperar_contrasena = findViewById(R.id.tv_recuperar_contrasena)

        correoET = findViewById(R.id.et_login_email)
        passwordET = findViewById(R.id.et_login_password)



        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        userService = retrofit.create(UsuarioApiService::class.java)

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
                Toast.makeText(this@LoginActivity,"Complete los campos" ,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            var user = LoginDTO(email = correoET.text.toString(),password = passwordET.text.toString())
            loginBtn.isClickable = false

            userService.generar_token(user).enqueue(object : Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if( response.headers().values("Authorization").isNullOrEmpty())
                    {
                        Toast.makeText(this@LoginActivity,"Correo o contraseña no existe" ,Toast.LENGTH_SHORT).show()
                        loginBtn.isClickable = true
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

                            if (usuario!!.data.status == 0){
                                Toast.makeText(this@LoginActivity, "La cuenta ha sido suspendida",Toast.LENGTH_LONG).show()
                                loginBtn.isClickable = true
                                return
                            }


                            Toast.makeText(this@LoginActivity, "Inicio de Sesión Correcto",Toast.LENGTH_LONG).show()



                            editor.putString("idusuario",usuario!!.data.idusuario.toString())
                            editor.putString("email",correoET.text.toString())
                            editor.putString("password",passwordET.text.toString())
                            editor.putString("name",usuario!!.data.firtsName )
                            editor.putString("apellido",usuario!!.data.lastName)
                            editor.putInt("idrole",usuario!!.data.role.idrol)
                            editor.putString("role",usuario!!.data.role.role)
                            editor.putString("status", usuario!!.data.status.toString())
                            editor.putString("fec_reg", usuario!!.data.fec_reg)
                            editor.apply()

                            val i = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(i)
                            loginBtn.isClickable = true
                            finish()
                        }

                        override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                            Log.i(TAG, "MAaaaal")
                            Toast.makeText(this@LoginActivity, "El servidor no responde, inténtelo más tarde", Toast.LENGTH_SHORT).show()
                            loginBtn.isClickable = true
                        }
                    })
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.i("Peticion de Token", "MAaaaal")
                    Toast.makeText(this@LoginActivity, "El servidor no responde, inténtelo más tarde", Toast.LENGTH_SHORT).show()
                    loginBtn.isClickable = true
                }
            })
        }

        recuperar_contrasena.setOnClickListener{
            mostrar_dialogo()
        }

    }



    override fun onBackPressed() {
        Shutdown.now(this, "Presione de nuevo para salir")
    }

    private fun mostrar_dialogo(){
        val builer = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view  = inflater.inflate(R.layout.dialog_password_recover,null)
        builer.setView(view)

        val dialog =  builer.create()
        dialog.show()

        val et_correo_recuperar = view.findViewById(R.id.et_correo_recuperar) as EditText


        btn_enviar_contra = view.findViewById(R.id.btn_enviar_recuperar_contrasena) as Button
        val btn_close_dialog = view.findViewById(R.id.btn_cerrar_recuperar_contrasena) as Button

        et_correo_recuperar.addTextChangedListener( object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (!validarEmail(p0.toString())) et_correo_recuperar.setError("Ingrese un correo válido")
                else et_correo_recuperar.setError(null)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        btn_enviar_contra.setOnClickListener {

            if(et_correo_recuperar.error.isNullOrEmpty() && !et_correo_recuperar.text.isNullOrEmpty()) {
                btn_enviar_contra.isClickable = false
                recuperarContrasena(et_correo_recuperar.text.toString())
                //dialog.dismiss()
            }
            else {
                Toast.makeText(this,"Ingrese un correo válido",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

        }

        btn_close_dialog.setOnClickListener {
            dialog.dismiss()
        }
    }



    fun recuperarContrasena(email: String){

        val retrofit = Retrofit.Builder().baseUrl(URL_API).addConverterFactory(GsonConverterFactory.create()).build()

        val emailService = retrofit.create(UsuarioApiService ::class.java)


        val Correo = Email(email = email)

        emailService.recuperar_contrasena(Correo).enqueue(object : Callback<ResponseDTO> {

            override fun onResponse(call: Call<ResponseDTO>, response: Response<ResponseDTO>) {
                val error = response.body()

                if (error!!.errorCode == "3")
                {
                    showDialogFalied()
                }
                if(error!!.errorCode == "0")
                {
                    showDialogSuccess()
                }
                Log.i("AHHH", Correo.toString())

            }
            override fun onFailure(call: Call<ResponseDTO>, t: Throwable) {
                Log.i("Resultado:", "MAaaaal")
                btn_enviar_contra.isClickable = true
            }
        })
    }
    fun validarEmail( email : String): Boolean{
        val pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    fun showDialogSuccess(){
    val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Confirmación")
        builder.setMessage("Por favor verifica tu correo electrónico, en unos instantes llegará el mensaje")
        builder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.dismiss()
            btn_enviar_contra.isClickable = true

        }).show()
    }

    fun showDialogFalied(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage("El correo que has ingresado no se encuentra registrado")
        builder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.dismiss()
            btn_enviar_contra.isClickable = true
        }).show()
    }

}

