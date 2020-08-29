package com.example.geoincendios.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.geoincendios.R
import com.example.geoincendios.activities.LoginActivity
import com.example.geoincendios.activities.MainActivity


class PerfilFragment : Fragment() {

    private lateinit var tv_nombre : TextView
    private lateinit var tv_correo : TextView
    private lateinit var tv_rol : TextView
    private lateinit var btn_cerrar_sesion : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  inflater.inflate(R.layout.fragment_perfil, container, false)

        val prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        val edit = prefs.edit()

        tv_nombre = view.findViewById(R.id.tv_nombre_perfil)
        tv_correo = view.findViewById(R.id.tv_correo_perfil)
        tv_rol = view.findViewById(R.id.tv_rol_perfil)

        btn_cerrar_sesion = view.findViewById(R.id.btn_cerrar_sesion)

        tv_nombre.setText(prefs.getString("name",""))
        tv_correo.setText(prefs.getString("email",""))
        tv_rol.setText(prefs.getString("role",""))

        btn_cerrar_sesion.setOnClickListener {
            edit.putString("email","")
            edit.putString("password","")
            edit.putString("token","")
            edit.apply()

            val i = Intent(context, LoginActivity::class.java)
            startActivity(i)

        }


        return  view
    }

    companion object {

    }
}