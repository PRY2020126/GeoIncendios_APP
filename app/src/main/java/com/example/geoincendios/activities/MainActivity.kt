package com.example.geoincendios.activities

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.fragments.ContribuirFragment
import com.example.geoincendios.fragments.GuardadosFragment
import com.example.geoincendios.fragments.MapsFragment
import com.example.geoincendios.fragments.PerfilFragment
import com.example.geoincendios.util.StateFragment
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG_ONE = "first"
private const val TAG_SECOND = "second"
private const val TAG_THIRD = "third"
private const val TAG_FOURTH = "fourth"
private const val MAX_HISTORIC = 5


class MainActivity : AppCompatActivity() {
/*

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //PreferenceManager
        val prefs = getSharedPreferences("user", Context.MODE_PRIVATE)

        val correito = prefs.getString("email","")
       //Toast.makeText(this,correito,Toast.LENGTH_SHORT).show()

        //Fragment por default
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

    private fun init() {

    }
    private fun loadFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction().also { fragmentTransaction ->
            fragmentTransaction.replace(R.id.fragmentContainer, fragment)
            fragmentTransaction.commit()

        }
    }

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
*/

    private val listState = mutableListOf<StateFragment>()
    private var currentTag: String = TAG_ONE
    private var oldTag: String = TAG_ONE
    private var currentMenuItemId: Int = R.id.navigationHome

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null){
            requestPermissionAndContinue()
        } //loadFirstFragment()
        val prefs = getSharedPreferences("user", Context.MODE_PRIVATE)

        init()
    }

    private fun requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("AAA", "permission denied, show dialog")
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            loadFirstFragment()
            this
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadFirstFragment()
            this
        } else {
            requestPermissionAndContinue()
        }
    }

    private fun init() {
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->

            if (currentMenuItemId != menuItem.itemId) {
                currentMenuItemId = menuItem.itemId

                when (currentMenuItemId) {
                    R.id.navigationHome -> changeFragment(TAG_ONE, MapsFragment.newInstance())
                    R.id.navigationContribuir -> changeFragment(TAG_SECOND, ContribuirFragment.newInstance())
                    R.id.navigationGuardado -> changeFragment(TAG_THIRD, GuardadosFragment.newInstance())
                    R.id.navigationPerfil -> changeFragment(TAG_FOURTH, PerfilFragment.newInstance())
                }

                return@setOnNavigationItemSelectedListener true
            }

            false
        }
    }

    private fun changeFragment(tagToChange: String, fragment: Fragment) {
        if (currentTag != tagToChange) {
            val ft = supportFragmentManager.beginTransaction()
            val currentFragment = supportFragmentManager.findFragmentByTag(currentTag)
            val fragmentToReplaceByTag = supportFragmentManager.findFragmentByTag(tagToChange)

            oldTag = currentTag
            currentTag = tagToChange

            if (fragmentToReplaceByTag != null) {
                currentFragment?.let { ft.hide(it).show(fragmentToReplaceByTag) }
            } else {
                currentFragment?.let { ft.hide(it).add(R.id.fragmentContainer, fragment, tagToChange) }
            }

            ft.commit()

            addBackStack()
        }
    }

    @ExperimentalStdlibApi
    override fun onBackPressed() {
        if (listState.size >= 1) {
            recoverFragment()
        } else {
            super.onBackPressed()
        }
    }

    @ExperimentalStdlibApi
    private fun recoverFragment() {

        val lastState = listState.last()

        val ft = supportFragmentManager.beginTransaction()

        val currentFragmentByTag = supportFragmentManager.findFragmentByTag(lastState.currentFragmentTag)
        val oldFragmentByTag = supportFragmentManager.findFragmentByTag(lastState.oldFragmentTag)

        if ((currentFragmentByTag != null && currentFragmentByTag.isVisible) &&
            (oldFragmentByTag != null && oldFragmentByTag.isHidden)) {
            ft.hide(currentFragmentByTag).show(oldFragmentByTag)
        }

        ft.commit()

        val menu = bottomNavigationView.menu

        when (lastState.oldFragmentTag) {
            TAG_ONE -> setMenuItem(menu.getItem(0))
            TAG_SECOND -> setMenuItem(menu.getItem(1))
            TAG_THIRD -> setMenuItem(menu.getItem(2))
            TAG_FOURTH -> setMenuItem(menu.getItem(3))
        }
        //Remove from Stack
        listState.removeLast()

        if (listState.isEmpty()) {
            currentTag = TAG_ONE
            oldTag = TAG_ONE
        } else {
            currentTag = listState.last().currentFragmentTag
            oldTag = listState.last().oldFragmentTag
        }
        updateLog()
    }

    private fun updateLog() {
    }

    private fun setMenuItem(menuItem: MenuItem) {
        menuItem.isChecked = true
        currentMenuItemId = menuItem.itemId
    }

    private fun loadFirstFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(
            R.id.fragmentContainer,
            MapsFragment.newInstance(),
            TAG_ONE)
        transaction.commit()
    }

    //Like YouTube
    private fun addBackStack() {
        updateLog()

        when (listState.size) {
            MAX_HISTORIC -> {

                listState[1].oldFragmentTag = TAG_ONE
                val firstState = listState[1]

                for (i in listState.indices) {
                    if (listState.indices.contains((i + 1))) {
                        listState[i] = listState[i + 1]
                    }
                }

                listState[0] = firstState
                listState[listState.lastIndex] = StateFragment(currentTag, oldTag)
            }
            else -> {
                listState.add(StateFragment(currentTag, oldTag))
            }
        }
    }



}