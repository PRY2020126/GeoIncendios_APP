package com.example.geoincendios

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.geoincendios.activities.Login
import com.example.geoincendios.activities.MainActivity
import kotlinx.android.synthetic.main.splash_screen.*


class Splash_screen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        iv_note.alpha =0f

        iv_note.animate().setDuration(700).alpha(1f).withEndAction(){
            val i = Intent(this, Login::class.java)
            startActivity(i)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

}
