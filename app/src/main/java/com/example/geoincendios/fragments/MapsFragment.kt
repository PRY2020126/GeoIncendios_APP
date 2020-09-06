package com.example.geoincendios.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.fragment.app.Fragment
import com.appolica.interactiveinfowindow.InfoWindow
import com.appolica.interactiveinfowindow.InfoWindowManager
import com.appolica.interactiveinfowindow.fragment.MapInfoWindowFragment
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.ZonaRiesgoApiService
import com.example.geoincendios.models.DTO.ZonaRiesgoDTO
import com.example.geoincendios.models.ZonaRiesgo
import com.example.geoincendios.persistence.DatabaseHandler
import com.example.geoincendios.persistence.ZonaRiesgoBD
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException
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

    private lateinit var searchText : EditText
    private lateinit var autocompleteFragment :AutocompleteSupportFragment

    //private lateinit var btn_guardar: Button

    private lateinit var db : DatabaseHandler

    private lateinit var imgBtn_recargar: ImageButton

    private lateinit var prefs: SharedPreferences

    private var listener : GuardadosFragment.BackPressedListener? = null

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        //var miLocation = LatLng(mLatitude,mLongitude)

        googleMap.setInfoWindowAdapter( object : GoogleMap.InfoWindowAdapter{
            override fun getInfoContents(p0: Marker?): View {
                    val v = layoutInflater.inflate(R.layout.marker,null)
                    val title = v.findViewById(R.id.marker_title) as TextView
                    title.setText(p0!!.title)
                    val btn_guardar = v.findViewById<Button>(R.id.btn_guardar_marker)
                    return v
            }
            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }
        })

        googleMap.setOnInfoWindowClickListener {
            var zona = ZonaRiesgoBD(markList.indexOf(it),it.title, it.position.latitude.toString(),it.position.longitude.toString())
            db.insertData(zona)
        }

        googleMap.setMaxZoomPreference(18f)
        googleMap.setOnCameraMoveListener {
            resizeMarks(googleMap)
        }
        val Lima = LatLng(-12.0554671, -77.0431111)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Lima, 11.0f))
    }


    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_maps,container,false)
        db = DatabaseHandler(context!!)

        imgBtn_recargar = activity!!.findViewById(R.id.imgBtn_recargar)

        prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
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
                    when(item.type!!.idtipo)
                    {
                        1 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_foreground) as BitmapDrawable }
                        2 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_medio_foreground) as BitmapDrawable}
                        3 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_bajo_foreground) as BitmapDrawable }
                        else -> { }
                    }
                    bitmap = bitmapdraw!!.bitmap
                    smallMaker= Bitmap.createScaledBitmap(bitmap,100,100,false)

                    val marker = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMaker))
                        .position(LatLng(item.latitude!!,item.longitude!!)).title(item.address).anchor(0.5f,0.5f).flat(false)
                    if(item.name != "") {
                        marker.snippet(item.name)
                    }
                    listMarcadores.add(marker)

                }
                Log.i("Marcadores",listMarcadores.toString())
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
        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
            com.google.android.libraries.places.api.model.Place.Field.NAME))
        autocompleteFragment.setCountry("pe")

        val searchBar =  autocompleteFragment.view!!.findViewById<EditText>(R.id.places_autocomplete_search_input)
        var last : Marker? = null
        var buscado : Boolean  = false

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onPlaceSelected(place: com.google.android.libraries.places.api.model.Place) {
                if(last != null)
                {
                    last!!.remove()
                    last = null
                    buscado = false
                }
                mapFragment?.getMapAsync(OnMapReadyCallback { googleMap ->
                    last = googleMap.addMarker(MarkerOptions().position(place.latLng!!).title(place.name))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng,13f))
                })
                buscado = true
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i("YOO", "An error occurred: $status")
            }
        })

        searchBar.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                if(searchBar.text.isBlank() && buscado)
                {
                    last!!.remove()
                    last = null
                    buscado = false
                    Log.i("Se limpio","AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

    }


    fun resizeMarks(googleMap: GoogleMap) {

         var pixelSizeAtZoom0 = 2
         var maxPixelSize = 150

        val zoom =  googleMap.cameraPosition.zoom
        if (zoomGlobal==0f)zoomGlobal = zoom

        //Log.i("Zoom",zoom.toString())
        //Toast.makeText(activity, googleMap.cameraPosition.zoom.toString(), Toast.LENGTH_SHORT).show()



        var relativePixelSize = Math.round(pixelSizeAtZoom0*Math.pow(1.45,zoom.toDouble()));
        //Log.i("RelativePixesSize",relativePixelSize.toString())
        if (relativePixelSize > maxPixelSize)
        {
            //relativePixelSize = maxPixelSize.toLong()
            //Log.i("RelativePixesSizeNuevo",relativePixelSize.toString())
        }


        if (abs(zoomGlobal - zoom) >= 0.5 )
        {

            var bitmapdraw :BitmapDrawable? = null
            var bitmap :Bitmap
            var smallMaker :Bitmap



            zoomGlobal = zoom
            //Log.i("Zoom","redibujandoooooooooooooooooooooooooooooooooooooooooooooooooooooo")
            for (marker in markList)
            {
                when (zonasRiesgoList[markList.indexOf(marker)].type!!.idtipo){
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


    fun mover(){
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val idmarker = prefs.getInt("idmarker",0)
        Log.i("IDMARKER", idmarker.toString())
        mapFragment?.getMapAsync({ googleMap ->
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markList.get(idmarker).position,16f))
            markList[idmarker].showInfoWindow()
        })
    }

}

