package com.example.geoincendios.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.fragments.MapsFragment
import com.example.geoincendios.fragments.PerfilFragment
import com.google.android.gms.maps.MapFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_maps.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Fragment por default
        loadFragment(MapsFragment())

        //Navbar
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when {
                menuItem.itemId == R.id.navigationHome -> {
                    loadFragment(MapsFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                menuItem.itemId == R.id.navigationReporte -> {
                    loadFragment(MapsFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                menuItem.itemId == R.id.navigationPerfil -> {
                    loadFragment(PerfilFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                else -> {
                    return@setOnNavigationItemSelectedListener false
                }
            }
        }
    }

    private fun loadFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction().also { fragmentTransaction ->
            fragmentTransaction.replace(R.id.fragmentContainer, fragment)
            fragmentTransaction.commit()

        }
    }

}