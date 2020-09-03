package com.example.geoincendios.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.geoincendios.R
import com.example.geoincendios.activities.LoginActivity
import com.example.geoincendios.activities.MainActivity
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.models.Role
import com.example.geoincendios.models.Usuario
import com.example.geoincendios.util.URL_API
import kotlinx.android.synthetic.main.password_change_dialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class PerfilFragment : Fragment() {

    private lateinit var tv_nombre : TextView
    private lateinit var tv_apellido : TextView
    private lateinit var tv_correo : TextView
    private lateinit var btn_cerrar_sesion : Button
    private lateinit var btn_cambiar_contrasena :Button

    private lateinit var et_new_password: EditText

    private lateinit var userService: UsuarioApiService

    private lateinit var prefs: SharedPreferences
    private  var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  inflater.inflate(R.layout.fragment_perfil, container, false)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        userService = retrofit.create(UsuarioApiService::class.java)


        prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        val edit = prefs.edit()

        token = prefs.getString("token","")!!

        tv_nombre = view.findViewById(R.id.tv_nombre_perfil)
        tv_apellido = view.findViewById(R.id.tv_apellido_perfil)
        tv_correo = view.findViewById(R.id.tv_correo_perfil)
        btn_cambiar_contrasena = view.findViewById(R.id.btn_editar_perfil)

        btn_cerrar_sesion = view.findViewById(R.id.btn_cerrar_sesion)

        tv_nombre.setText(prefs.getString("name",""))
        tv_apellido.setText(prefs.getString("apellido",""))
        tv_correo.setText(prefs.getString("email",""))

        btn_cerrar_sesion.setOnClickListener {
            edit.clear()
            edit.commit()
            val i = Intent(context, LoginActivity::class.java)
            startActivity(i)
            activity!!.finish()
        }

        btn_cambiar_contrasena.setOnClickListener {
            mostrar_dialogo()
        }

        return  view
    }


    private fun cambiar_contrasena(){
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
        val currentDate = sdf.format(Date())

        val user = Usuario(idusuario = prefs.getString("idusuario","")!!, firtsName = prefs.getString("name","")!!,lastName = prefs.getString("apellido","")!!,
            email = prefs.getString("email","")!!,password = et_new_password.text.toString(),role = Role(idrol = prefs.getInt("idrole",3)!!,role = ""),
            status = 1, user_reg = "",fec_reg ="" , cpc_reg = null,user_mod = null,cpc_mod = null,fec_mod = currentDate )

        Toast.makeText(activity,currentDate,Toast.LENGTH_LONG).show()
        Toast.makeText(activity,user.toString(),Toast.LENGTH_LONG).show()

        userService.editarPerfil(token!!,user).enqueue(object : Callback<UserDTO> {
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                val usuario = response.body()
                Log.i("AHHHH",response.body().toString())

            }
            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                Log.i("AHHH", "MAaaaal")
            }
        })
    }

    private fun mostrar_dialogo(){
        val builer = AlertDialog.Builder(activity)
        val inflater = layoutInflater
        val view  = inflater.inflate(R.layout.password_change_dialog,null)
        builer.setView(view)

        val dialog =  builer.create()
        dialog.show()

        val et_contra_acutal = view.findViewById(R.id.et_actual_password) as EditText
        et_new_password = view.findViewById(R.id.et_new_password)
        val et_new_password_confirm = view.findViewById(R.id.et_new_password_confirm) as EditText

        val btn_cambiar_contra = view.findViewById(R.id.btn_cambiar_contrasena) as Button
        val btn_close_dialog = view.findViewById(R.id.btn_close_dialog_contrasena) as Button

        btn_cambiar_contra.setOnClickListener {
            if(et_contra_acutal.text.toString() != prefs.getString("password",""))
            {
                Toast.makeText(activity,"La contraseña actual es incorrecta",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            cambiar_contrasena()
            dialog.dismiss()
            btn_cerrar_sesion.performClick()
        }
        btn_close_dialog.setOnClickListener {
            dialog.dismiss()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = PerfilFragment()
    }
}