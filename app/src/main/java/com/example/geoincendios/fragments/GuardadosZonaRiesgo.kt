package com.example.geoincendios.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.geoincendios.R
import com.example.geoincendios.persistence.ClickListener
import com.example.geoincendios.persistence.DatabaseHandler
import com.example.geoincendios.persistence.ZonaRiesgoAdapter
import com.example.geoincendios.persistence.ZonaRiesgoBD
import java.lang.RuntimeException

class GuardadosZonaRiesgo : Fragment(){

    private lateinit var RVGuardados : RecyclerView
    private lateinit var layoutManager : RecyclerView.LayoutManager
    private lateinit var guardados: MutableList<ZonaRiesgoBD>
    private lateinit var adaptador : ZonaRiesgoAdapter


    lateinit var prefs : SharedPreferences
    lateinit var edit : SharedPreferences.Editor

    private lateinit var db : DatabaseHandler

    var  backPressedListener : GuardadosFragment.BackPressedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guardados_zonas,container,false)

        prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        edit = prefs.edit()
        db = DatabaseHandler(context!!)
        guardados = db.readData()

        RVGuardados = view.findViewById(R.id.rv_guardados)
        layoutManager = LinearLayoutManager(context)
        adaptador = ZonaRiesgoAdapter(guardados, object: ClickListener {
            override fun onClick(view: View, position: Int) {
                edit.putInt("idmarker",guardados[position].id)
                edit.commit()
                Log.i("Marcador",guardados[position].toString())
                backPressedListener?.onItemClick()
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
        fun newInstance() = GuardadosZonaRiesgo()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GuardadosFragment.BackPressedListener){
            backPressedListener = context as GuardadosFragment.BackPressedListener
        } else {
            throw  RuntimeException(context.toString())
        }
    }

}