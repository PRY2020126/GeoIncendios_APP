package com.example.geoincendios.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.fragments.MapsFragment
import kotlinx.android.synthetic.main.fragment_maps.*

class MainActivity : AppCompatActivity() {

    var FragmentMap = MapsFragment()
    val manager = supportFragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var change = findViewById<Button>(R.id.button_change)

        change.setOnClickListener {
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.fragment_main, FragmentMap)
            transaction.commit()
        }

    }


}