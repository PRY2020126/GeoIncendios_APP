package com.example.geoincendios.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geoincendios.R
import com.example.geoincendios.persistence.*
import java.lang.RuntimeException

class GuardadosPersonalizado: Fragment() {


    private lateinit var RVGuardados : RecyclerView
    private lateinit var layoutManager : RecyclerView.LayoutManager
    private lateinit var guardados: MutableList<ZonaRiesgoBD>
    private lateinit var adaptador : ZonaPersonalizadaAdapter


    lateinit var prefs : SharedPreferences
    lateinit var edit : SharedPreferences.Editor

    private lateinit var db : DatabaseHandler

    var  redirectToMap : GuardadosFragment.RedirectToMapsPer? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_guardados_personalizados,container,false)

        prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        edit = prefs.edit()
        db = DatabaseHandler(context!!)
        guardados = db.readDataPer()

        RVGuardados = view.findViewById(R.id.rv_guardados_personalizados)
        layoutManager = LinearLayoutManager(context)
        adaptador = ZonaPersonalizadaAdapter(guardados, object: ClickListener {
            override fun onClick(view: View, position: Int) {

                val builder = AlertDialog.Builder(context!!)
                builder.setMessage("¿Que desea hacer?")
                    .setCancelable(true)
                    .setPositiveButton("Ir a la zona personalizada", DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                        edit.putString("lat",guardados[position].lat)
                        edit.putString("lng",guardados[position].lng)
                        edit.putString("add",guardados[position].addres)
                        edit.commit()
                        redirectToMap?.onItemClickPer()
                    })
                    builder.setNegativeButton("Eliminar",  { dialogInterface, i ->
                        db.deleteZonaPersonalizada(guardados[position].addres)
                        dialogInterface.dismiss()
                        val fragment = fragmentManager!!.findFragmentById(R.id.frame_layout_guardados)
                        val ft = fragmentManager!!.beginTransaction()
                        ft.detach(fragment!!)
                        ft.attach(fragment!!)
                        ft.commit()
                    })
                    builder.show()
            }
        })

        RVGuardados.layoutManager = layoutManager
        RVGuardados.adapter = adaptador

        return view



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GuardadosPersonalizado()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GuardadosFragment.RedirectToMapsPer){
            redirectToMap = context
        } else {
            throw  RuntimeException(context.toString())
        }
    }

}