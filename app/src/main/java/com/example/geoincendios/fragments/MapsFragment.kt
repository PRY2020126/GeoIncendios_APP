package com.example.geoincendios.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.provider.VoicemailContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.ZonaRiesgoApiService
import com.example.geoincendios.models.DTO.ZonaRiesgoDTO
import com.example.geoincendios.util.URL_API
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.Places
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
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



    //private lateinit var autoCompleteSuport :AutocompleteSupportFragment

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
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

        com.google.android.libraries.places.api.Places.initialize(context!!, context!!.resources.getString(R.string.google_maps_key) );

        token = prefs.getString("token","")
        marcadores = arrayListOf()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        //getLastLocation()

        return view

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        Traer_Marcadores(token!!)
        Buscador(mapFragment!!)
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
                            .anchor(0.5f,0.5f).position(item).title("Zona de Riesgo").flat(false))
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


    private fun Buscador( mapFragment: SupportMapFragment){
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
            com.google.android.libraries.places.api.model.Place.Field.NAME))
        var last : Marker? = null
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onPlaceSelected(place: com.google.android.libraries.places.api.model.Place) {
                if(last != null)
                {
                    last!!.remove()
                    last = null
                }
                mapFragment?.getMapAsync(OnMapReadyCallback { googleMap ->
                    last = googleMap.addMarker(MarkerOptions().position(place.latLng!!).title(place.name))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng,13f))
                })
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("YOO", "An error occurred: $status")
            }
        })
    }

}

