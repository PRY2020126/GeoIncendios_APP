package com.example.geoincendios.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.ZonaRiesgoApiService
import com.example.geoincendios.models.DTO.ZonaRiesgoDTO
import com.example.geoincendios.models.ZonaRiesgo
import com.example.geoincendios.util.URL_API
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.android.synthetic.main.fragment_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.abs

class MapsFragment : Fragment() {


    private lateinit var listMarcadores : MutableList<MarkerOptions>
    private lateinit var markList : MutableList<Marker>
    private lateinit var zonasRiesgoList: MutableList<ZonaRiesgo>

    private var token : String? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    protected var mLastLocation: Location? = null

    private  var zoomGlobal = 0f

    private var mLatitude: Double = 1.0
    private var mLongitude: Double = 1.0

    private lateinit var imgBtn_recargar: ImageButton

    // private var zoom = 0f

    //private lateinit var autoCompleteSuport :AutocompleteSupportFragment



    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        //var miLocation = LatLng(mLatitude,mLongitude)

        googleMap.setMaxZoomPreference(18f)

        googleMap.setOnCameraMoveListener {
            resizeMarks(googleMap)
        }

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

        imgBtn_recargar = activity!!.findViewById(R.id.imgBtn_recargar)

        val prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        com.google.android.libraries.places.api.Places.initialize(context!!, context!!.resources.getString(R.string.google_maps_key) )
        token = prefs.getString("token","")
        listMarcadores = arrayListOf()
        markList = arrayListOf()
        zonasRiesgoList = arrayListOf()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)

        //getLastLocation()



        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        Traer_Marcadores(token!!,mapFragment!!)
        Buscador(mapFragment!!)

        imgBtn_recargar.setOnClickListener {
            recargar(mapFragment)
        }

    }

    private fun Traer_Marcadores(token:String,mapFragment: SupportMapFragment): List<Marker>{
       zonasRiesgoList.clear()
       listMarcadores.clear()
       for(item in markList){
           item.remove()
       }
       markList.clear()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val zonaRiesgoService = retrofit.create(ZonaRiesgoApiService::class.java)

        zonaRiesgoService.getZonasRiesgo(token).enqueue(object : Callback<ZonaRiesgoDTO> {
            override fun onResponse(call: Call<ZonaRiesgoDTO>, response: Response<ZonaRiesgoDTO>) {
                val zonas = response.body()

                var bitmapdraw : BitmapDrawable? = null
                var bitmap:Bitmap
                var smallMaker :Bitmap

                if (zonas == null)return


                for (item in zonas!!.data){
                    zonasRiesgoList.add(item)
                    when(item.type.idtipo)
                    {
                        1 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_foreground) as BitmapDrawable }
                        2 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_medio_foreground) as BitmapDrawable}
                        3 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_bajo_foreground) as BitmapDrawable }
                        else -> { }
                    }
                    bitmap = bitmapdraw!!.bitmap
                    smallMaker= Bitmap.createScaledBitmap(bitmap,100,100,false)

                    val marker = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMaker))
                        .position(LatLng(item.latitude,item.longitude)).title(item.name).anchor(0.5f,0.5f).flat(false).snippet(item.address)

                    listMarcadores.add(marker)
                }

                mapFragment?.getMapAsync(OnMapReadyCallback { googleMap ->
                    for (item in listMarcadores){
                        markList.add(googleMap.addMarker(item))
                    }
                })
            }

            override fun onFailure(call: Call<ZonaRiesgoDTO>, t: Throwable) {
                Log.i("AAAA", "MAaaaal")
            }
        })

        return markList
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



    fun resizeMarks(googleMap: GoogleMap) {

         var pixelSizeAtZoom0 = 2
         var maxPixelSize = 150

        val zoom =  googleMap.cameraPosition.zoom
        if (zoomGlobal==0f)zoomGlobal = zoom

        Log.i("Zoom",zoom.toString())
        //Toast.makeText(activity, googleMap.cameraPosition.zoom.toString(), Toast.LENGTH_SHORT).show()



        var relativePixelSize = Math.round(pixelSizeAtZoom0*Math.pow(1.45,zoom.toDouble()));
        Log.i("RelativePixesSize",relativePixelSize.toString())
        if (relativePixelSize > maxPixelSize)
        {
            //relativePixelSize = maxPixelSize.toLong()
            Log.i("RelativePixesSizeNuevo",relativePixelSize.toString())
        }


        if (abs(zoomGlobal - zoom) >= 0.5 )
        {

            var bitmapdraw :BitmapDrawable? = null
            var bitmap :Bitmap
            var smallMaker :Bitmap



            zoomGlobal = zoom
            Log.i("Zoom","redibujandoooooooooooooooooooooooooooooooooooooooooooooooooooooo")
            for (marker in markList)
            {
                when (zonasRiesgoList[markList.indexOf(marker)].type.idtipo){
                    1-> {  bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_foreground) as BitmapDrawable }
                    2-> {  bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_medio_foreground) as BitmapDrawable  }
                    3-> {  bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_bajo_foreground) as BitmapDrawable }
                }

                bitmap = bitmapdraw!!.bitmap
                smallMaker = Bitmap.createScaledBitmap(bitmap,relativePixelSize.toInt(),relativePixelSize.toInt(),false)
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMaker))
            }
        }
     }

    fun recargar(mapFragment: SupportMapFragment){
        val rotate = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        rotate.setDuration(1000)
        rotate.setRepeatCount(Animation.INFINITE)
        rotate.setRepeatMode(Animation.INFINITE)
        rotate.setInterpolator(LinearInterpolator())
        imgBtn_recargar.startAnimation(rotate)
        Toast.makeText(activity,"Recargando...",Toast.LENGTH_SHORT).show()
        Traer_Marcadores(token!!,mapFragment)
        //activity!!.finish()
        //startActivity(activity!!.intent)

        Handler().postDelayed({
            imgBtn_recargar.clearAnimation()
        },2000)

    }


}

