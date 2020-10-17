package com.example.geoincendios.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.geoincendios.R
import com.example.geoincendios.activities.LoginActivity
import com.example.geoincendios.activities.MainActivity
import com.example.geoincendios.activities.RecomendacionesActivity
import com.example.geoincendios.activities.WebViewActivity
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.models.Role
import com.example.geoincendios.models.Usuario
import com.example.geoincendios.util.URL_API
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_perfil.*
import kotlinx.android.synthetic.main.password_change_dialog.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class PerfilFragment : Fragment() {

    private lateinit var tv_nombre : TextView
    private lateinit var tv_apellido : TextView
    private lateinit var tv_correo : TextView
    private lateinit var btn_ultimas_emergencias : Button
    private lateinit var btn_recomendaciones : Button
    private lateinit var btn_cambiar_contrasena :Button
    private lateinit var chb_segundoplano : CheckBox

    private lateinit var et_new_password: EditText

    private lateinit var userService: UsuarioApiService

    private lateinit var prefs: SharedPreferences
    private lateinit var edit : SharedPreferences.Editor
    private  var token: String = ""

    var servicioState : ServicioState? = null

    private lateinit var btn_cambiar_contra:Button

    private lateinit var imgBtn_cerrar_sesion: ImageButton


    interface ServicioState{
        fun activarServicio()
        {

        }
        fun desactivarServicio()
        {

        }
    }




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
        edit = prefs.edit()

        token = prefs.getString("token","")!!



        tv_nombre = view.findViewById(R.id.tv_nombre_perfil)
        tv_apellido = view.findViewById(R.id.tv_apellido_perfil)
        tv_correo = view.findViewById(R.id.tv_correo_perfil)
        btn_cambiar_contrasena = view.findViewById(R.id.btn_editar_perfil)
        btn_recomendaciones = view.findViewById(R.id.btn_recomendaciones)
        chb_segundoplano = view.findViewById(R.id.chb_segundoplano)

        btn_ultimas_emergencias = view.findViewById(R.id.btn_ultimas_emergencias)

        imgBtn_cerrar_sesion = view.findViewById(R.id.imgBtn_cerrar_sesion)
        tv_nombre.setText(prefs.getString("name",""))
        tv_apellido.setText(prefs.getString("apellido",""))
        tv_correo.setText(prefs.getString("email",""))

        Log.i("Service",prefs.getBoolean("service",false).toString())

        chb_segundoplano.isChecked = prefs.getBoolean("service",false)

        btn_recomendaciones.setOnClickListener {
            val i = Intent(context, RecomendacionesActivity::class.java)
            startActivity(i)
        }

        btn_ultimas_emergencias.setOnClickListener {
            val i = Intent(context, WebViewActivity::class.java)
            startActivity(i)
        }

        btn_cambiar_contrasena.setOnClickListener {
            mostrar_dialogo()
        }

        imgBtn_cerrar_sesion.setOnClickListener{
            showDialogCerrarSesion()
        }

        chb_segundoplano.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if(chb_segundoplano.isChecked)
                {
                    val locatioManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (!locatioManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        chb_segundoplano.isChecked = false
                        Toast.makeText(activity, "Active el GPS Primero",Toast.LENGTH_SHORT).show()
                        return
                    }
                    servicioState!!.activarServicio()
                    edit.putBoolean("service", true)
                    edit.commit()
                }
                else
                {
                    servicioState!!.desactivarServicio()
                    edit.putBoolean("service", false)
                    edit.commit()

                }
            }
        })

        return  view
    }

    fun showDialogCerrarSesion()
    {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(true)
        builder.setTitle("Cierre de Sesión")
        builder.setMessage("¿Esta seguro de cerrar sesión?")
        builder.setPositiveButton("Si", DialogInterface.OnClickListener { dialogInterface, i ->
            edit.clear()
            edit.commit()
            val i = Intent(context, LoginActivity::class.java)
            startActivity(i)
            activity!!.finish()
        }).
        setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.dismiss()
        }) .show()
    }

    fun showDialogSuccess(){
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle("Cambio de contraseña exitoso")
        builder.setMessage("Por favor vuelva a iniciar sesion con su nueva contraseña")
        builder.setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
            dialogInterface.dismiss()
            edit.clear()
            edit.commit()
            val i = Intent(context, LoginActivity::class.java)
            startActivity(i)
            activity!!.finish()
        }).show()
    }

    private fun cambiar_contrasena(){
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
        val currentDate = sdf.format(Date())

        val user = Usuario(idusuario = prefs.getString("idusuario","")!!, firtsName = prefs.getString("name","")!!,lastName = prefs.getString("apellido","")!!,
            email = prefs.getString("email","")!!,password = et_new_password.text.toString(),role = Role(idrol = prefs.getInt("idrole",3)!!,role = ""),
            status = 1, user_reg = "",fec_reg = prefs.getString("fec_reg","") , cpc_reg = null,user_mod = null,cpc_mod = null,fec_mod = currentDate )

        //Toast.makeText(activity,currentDate,Toast.LENGTH_LONG).show()


        userService.editarPerfil(token!!,user).enqueue(object : Callback<UserDTO> {

            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                val usuario = response.body()
                Log.i("Usuario",user.toString())
                Log.i("Actualizar Perfil",response.body().toString())
                
                showDialogSuccess()
            }
            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                Log.i("Resultado", "Ah ocurrido un error")
                btn_cambiar_contra.isClickable = true
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
            if(et_contra_acutal.text.isNullOrEmpty() || et_new_password.text.isNullOrEmpty() || et_new_password_confirm.text.isNullOrEmpty())
            {
                Toast.makeText(activity,"Complete todos los campos",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(et_contra_acutal.text.toString() != prefs.getString("password",""))
            {
                Toast.makeText(activity,"La contraseña actual es incorrecta",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(et_new_password.text.toString() != et_new_password_confirm.text.toString())
            {
                Toast.makeText(activity,"La contraseña nueva no coincide",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            btn_cambiar_contra.isClickable = false
            cambiar_contrasena()
        }
        btn_close_dialog.setOnClickListener {
            dialog.dismiss()
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = PerfilFragment()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ServicioState){
            servicioState = context
        } else {
            throw  RuntimeException(context.toString())
        }
    }
}