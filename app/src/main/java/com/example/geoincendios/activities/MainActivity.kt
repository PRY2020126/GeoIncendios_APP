package com.example.geoincendios.activities

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.fragments.ContribuirFragment
import com.example.geoincendios.fragments.MapsFragment
import com.example.geoincendios.fragments.PerfilFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val FINE_LOCATION_RQ = 101
    val CAMERA_RQ = 102
    //val MY_LOCATION = 99

    private lateinit var map_fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Fragment por default
        //checkForPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION,"location",FINE_LOCATION_RQ)
        requestPermissionAndContinue()


        //Navbar
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when {
                menuItem.itemId == R.id.navigationHome -> {
                    loadFragment(MapsFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                menuItem.itemId == R.id.navigationContribuir -> {
                    loadFragment(ContribuirFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                menuItem.itemId == R.id.navigationGuardado -> {
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

    /* fun checkForPermissions(permission: String, name:String,requestCode:Int){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when{
                ContextCompat.checkSelfPermission(applicationContext,permission) == PackageManager.PERMISSION_GRANTED ->{
                    Toast.makeText(applicationContext, "$name permission granted",Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission,name,requestCode)
                else -> ActivityCompat.requestPermissions(this,arrayOf(permission),requestCode)
            }
        }

    }*/

   /* override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        fun innerCheck(name: String){
            if(grantResults.isEmpty()||grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,"$name permission refused", Toast.LENGTH_SHORT).show()
            } else{
                Toast.makeText(applicationContext,"$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        when(requestCode){
            FINE_LOCATION_RQ -> innerCheck("location")
            CAMERA_RQ -> innerCheck("camera")
        }
    }*/


  /*  private  fun showDialog(permission: String,name: String,requestCode: Int){
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to acces your $name is requiered")
            setTitle("Permission requiered")
            setPositiveButton("OK"){ dialog, which ->
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission),requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()


    }
*/


    private fun requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("AAA", "permission denied, show dialog")
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            loadFragment(MapsFragment())
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadFragment(MapsFragment())
        } else {
            requestPermissionAndContinue()
        }
    }




}