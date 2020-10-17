package com.example.geoincendios.activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.emmanuelkehinde.shutdown.Shutdown
import com.example.geoincendios.MyService
import com.example.geoincendios.R
import com.example.geoincendios.Splash_screen
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


class MainActivity : AppCompatActivity(), GuardadosFragment.BackPressedListener, GuardadosFragment.RedirectToMapsPer, PerfilFragment.ServicioState {


    private val listState = mutableListOf<StateFragment>()
    private var currentTag: String = TAG_ONE
    private var oldTag: String = TAG_ONE
    private var currentMenuItemId: Int = R.id.navigationHome

    private lateinit var locatioManager: LocationManager

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "com.example.geoincendios.activities"
    private val descrption = "Test notification"

    private lateinit var fragmentManager: FragmentManager

    private lateinit var myService: MyService


    override fun activarServicio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, myService::class.java))
        } else {
            startService(Intent(this, myService::class.java))
        }
    }

    override fun desactivarServicio() {
        stopService(Intent(this, myService::class.java))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefs = getSharedPreferences("user", Context.MODE_PRIVATE)

        init()
        myService = MyService()


        /*if(prefs.getBoolean("service",false) && !isMyServiceRunning(myService::class.java))
        {
            activarServicio()
        }*/

        locatioManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locatioManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertNoGps()
        }
        else
        {
            if (savedInstanceState == null && Build.VERSION.SDK_INT >= 23) {
                requestPermissionAndContinue()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 111)
        {
            if (!locatioManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                AlertNoGps()
            }
            else
            {
                Log.i("Vuelve", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                requestPermissionAndContinue()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun  AlertNoGps(){
        val builder = AlertDialog.Builder(this)
        builder.setMessage("EL sistema GPS no esta activado,¿Desea activarlo?")
            .setCancelable(false)
            .setPositiveButton("Si", DialogInterface.OnClickListener { dialogInterface, i ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(intent,111)
            })
            .setNegativeButton("No",  { dialogInterface, i ->
                dialogInterface.dismiss()
                requestPermissionAndContinue()
            }).show()
    }


    fun mostrarDialogoPermisos() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Ha rechazado los permisos de la aplicación, por favor activelos manualmente y reinicie la aplicación para poder usarla")
            .setCancelable(false)
            .setPositiveButton("Ir Ahora", DialogInterface.OnClickListener { dialogInterface, i ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri: Uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                this.startActivityForResult(intent,222)
            })
            .setNegativeButton("Entiendo",{dialogInterface, i ->
                dialogInterface.dismiss()
            }).show()
    }

    private fun requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.e("Error", "permission denied, show dialog")
                mostrarDialogoPermisos()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
                loadFirstFragment()
            }
            this
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode)
        {
            -1 -> {}
            1 -> {
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        loadFirstFragment()
                        this
                 }  else {
                    requestPermissionAndContinue()
                        this
                    }
            }
        }

    }

    private fun createNoti(){
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            if(!notificationManager.areNotificationsEnabled())
            {
                openNotificationSettings()
            }
        }
        else
        {
            if(!NotificationManagerCompat.from(this).areNotificationsEnabled())
            {
                openNotificationSettings()
            }
        }

        val intent = Intent(this, Splash_screen::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationChannel = NotificationChannel(channelId,"Canal 1",NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)

            val bitmap = BitmapFactory.decodeResource(resources,R.drawable.logo)

            builder = Notification.Builder(this,channelId)
                .setContentTitle("Alerta")
                .setContentText("Se encuentra dentro de una zona de riesgo")
                .setSmallIcon(R.drawable.logo)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
        }
        else {
            val bitmap = BitmapFactory.decodeResource(resources,R.drawable.logo)

            builder = Notification.Builder(this)
                .setContentTitle("Alerta")
                .setContentText("Se encuentra dentro de una zona de riesgo")
                .setSmallIcon(R.drawable.logo)
                .setColor(Color.RED)
                .setLargeIcon(bitmap)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234,builder.build())

    }

    private fun openNotificationSettings(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            var intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(intent)
        } else {
            /*var intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.setData(Uri.parse("package:"+packageName))*/
            var intent = Intent()
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS")
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
            startActivity(intent)
        }
    }


    private fun init() {


        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            if (currentMenuItemId != menuItem.itemId) {
                currentMenuItemId = menuItem.itemId

                when (currentMenuItemId) {
                    R.id.navigationHome -> {changeFragment(TAG_ONE, MapsFragment.newInstance())}
                    R.id.navigationContribuir -> {changeFragment(TAG_SECOND, ContribuirFragment.newInstance())}
                    R.id.navigationGuardado -> {changeFragment(TAG_THIRD, GuardadosFragment.newInstance())}
                    R.id.navigationPerfil -> {changeFragment(TAG_FOURTH, PerfilFragment.newInstance())}
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
            Shutdown.now(this, "Presione de nuevo para salir")
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
            TAG_ONE -> {setMenuItem(menu.getItem(0))}
            TAG_SECOND -> {setMenuItem(menu.getItem(1))}
            TAG_THIRD -> {setMenuItem(menu.getItem(2))}
            TAG_FOURTH -> {setMenuItem(menu.getItem(3))}

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
        transaction.commitAllowingStateLoss()
        //transaction.commit()
    }

    private fun addBackStack() {


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

    override fun onItemClick() {
        changeFragment(TAG_ONE, MapsFragment.newInstance())
        val menu = bottomNavigationView.menu
        setMenuItem(menu.getItem(0))
        val a = supportFragmentManager.findFragmentByTag(currentTag) as MapsFragment
        a.mover()
    }

    override fun onItemClickPer() {
        changeFragment(TAG_ONE, MapsFragment.newInstance())
        val menu = bottomNavigationView.menu
        setMenuItem(menu.getItem(0))
        val a = supportFragmentManager.findFragmentByTag(currentTag) as MapsFragment
        a.moverPer()
    }

}