package com.example.geoincendios.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.ZonaRiesgoApiService
import com.example.geoincendios.models.DTO.ZonaRiesgoDTO
import com.example.geoincendios.util.URL_API
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsFragment : Fragment() {


    private lateinit var marcadores:MutableList<LatLng>
    private var token : String? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    protected var mLastLocation: Location? = null


    private var mLatitude: Double = 1.0
    private var mLongitude: Double = 1.0


    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        //var miLocation = LatLng(mLatitude,mLongitude)

        val Lima = LatLng(-12.0554671, -77.0431111)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Lima, 11.0f))

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful && task.result != null) {
                    mLastLocation = task.result
                    mLatitude =  mLastLocation!!.latitude
                    mLongitude = mLastLocation!!.longitude

                } else {
                    Log.w("AEA", "getLastLocation:exception", task.exception)
                }
            }
    }


    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_maps,container,false)
        val prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)

        token = prefs.getString("token","")
        marcadores = arrayListOf()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getLastLocation()

        return view

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        Traer_Marcadores(token!!)
    }


   private fun Traer_Marcadores(token:String): List<LatLng>{

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val zonaRiesgoService = retrofit.create(ZonaRiesgoApiService::class.java)

        zonaRiesgoService.getZonasRiesgo(token).enqueue(object : Callback<ZonaRiesgoDTO> {
            override fun onResponse(call: Call<ZonaRiesgoDTO>, response: Response<ZonaRiesgoDTO>) {
                val zonas = response.body()

                for (item in zonas!!.data){
                    marcadores!!.add(LatLng(item.latitude,item.longitude))
                }

                Toast.makeText(activity, marcadores.toString(),Toast.LENGTH_SHORT).show()

                val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync(OnMapReadyCallback { googleMap ->
                    for (item in marcadores) {
                        googleMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_riesgo_foreground))
                            .anchor(0.0f,1.0f).position(item).title("Zona de Riesgo"))
                    }
                })
            }

            override fun onFailure(call: Call<ZonaRiesgoDTO>, t: Throwable) {
                Log.i("AAAA", "MAaaaal")
            }
        })

        return marcadores!!
    }

    companion object{
        @JvmStatic
        fun newInstance() = MapsFragment()
    }

}

