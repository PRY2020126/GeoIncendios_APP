package com.example.geoincendios.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.geoincendios.R
import com.example.geoincendios.persistence.DatabaseHandler
import com.example.geoincendios.persistence.ZonaRiesgoAdapter
import com.example.geoincendios.persistence.ZonaRiesgoBD
import com.google.android.material.bottomnavigation.BottomNavigationView



open class GuardadosFragment : Fragment() {

    lateinit var guardadosZonaRiesgo: GuardadosZonaRiesgo
    lateinit var guardadosPersonalizado: GuardadosPersonalizado


    interface BackPressedListener{
        fun onItemClick()
    }

    interface RedirectToMapsPer{
        fun onItemClickPer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_guardados, container, false)


        guardadosZonaRiesgo = GuardadosZonaRiesgo()
        fragmentManager!!.beginTransaction().replace(R.id.frame_layout_guardados, guardadosZonaRiesgo).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()


        val topNavigationView = view.findViewById<BottomNavigationView>(R.id.topNavigationView)

        topNavigationView.setOnNavigationItemSelectedListener { item ->

            when (item.itemId){
                R.id.sub_navigationZonasRiesgo -> {
                    guardadosZonaRiesgo = GuardadosZonaRiesgo()
                    fragmentManager!!.beginTransaction().replace(R.id.frame_layout_guardados, guardadosZonaRiesgo).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                }
                R.id.sub_navigationPersonalizados -> {
                    guardadosPersonalizado = GuardadosPersonalizado()
                    fragmentManager!!.beginTransaction().replace(R.id.frame_layout_guardados, guardadosPersonalizado).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit()
                }
            }
            true
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = GuardadosFragment()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val fragment = fragmentManager!!.findFragmentById(R.id.frame_layout_guardados)
        val ft = fragmentManager!!.beginTransaction()
        ft.detach(fragment!!)
        ft.attach(fragment!!)
        ft.commit()
    }





}