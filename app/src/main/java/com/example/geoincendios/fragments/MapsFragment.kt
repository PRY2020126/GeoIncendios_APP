package com.example.geoincendios.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.interfaces.RecursosApiService
import com.example.geoincendios.interfaces.ZonaRiesgoApiService
import com.example.geoincendios.models.DTO.RecursosDTO
import com.example.geoincendios.models.DTO.ZonaRiesgoDTO
import com.example.geoincendios.models.Recurso
import com.example.geoincendios.models.ZonaRiesgo
import com.example.geoincendios.persistence.DatabaseHandler
import com.example.geoincendios.persistence.ZonaRiesgoBD
import com.example.geoincendios.util.URL_API
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
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
import kotlin.math.abs

class MapsFragment : Fragment() {


    private lateinit var listMarcadores : MutableList<MarkerOptions>
    private lateinit var markList : MutableList<Marker>
    private lateinit var zonasRiesgoList: MutableList<ZonaRiesgo>
    var riesgoArray : MutableList<Int> = arrayListOf()

    var recursosArray : MutableList<MarkerOptions> = arrayListOf()
    var recursosMarkerArray : MutableList<Marker> = arrayListOf()


    private var token : String? = null

    private  var zoomGlobal = 0f

    private lateinit var db : DatabaseHandler

    private lateinit var imgBtn_recargar: ImageButton

    private lateinit var prefs: SharedPreferences
    private lateinit var locationService : FusedLocationProviderClient

    var markerZonaPer : MarkerOptions? = null
    var last : Marker? = null

    var markerPer : Marker? = null

    private  lateinit var mylocation : LatLng

    private lateinit var locationRequest  : LocationRequest
    private lateinit var locationCallback  : LocationCallback
    private lateinit var mFusedLocationClient  : FusedLocationProviderClient


    private var hidrantesMostrados: Boolean = false

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        googleMap.setInfoWindowAdapter( object : GoogleMap.InfoWindowAdapter{
            override fun getInfoContents(p0: Marker?): View? {
                if (p0 in recursosMarkerArray)
                {
                    return null
                }
                    val v = layoutInflater.inflate(R.layout.marker,null)
                    val title = v.findViewById(R.id.marker_title) as TextView
                    val detalles = v.findViewById(R.id.marker_snniped) as TextView
                    //Log.i("detalle", p0!!.snippet.toString())
                    if(p0!!.snippet.isNullOrEmpty()){
                        detalles.visibility = View.GONE
                    }
                    else detalles.setText(p0!!.snippet)

                    title.setText(p0!!.title)
                    val btn_guardar = v.findViewById<Button>(R.id.btn_guardar_marker)
                    return v
            }
            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }
        })

        googleMap.setOnInfoWindowClickListener {
            if(it == last)
            {
                val zona = ZonaRiesgoBD(markList.indexOf(it),it.title, it.position.latitude.toString(),it.position.longitude.toString(),0)
                db.insertDataPer(zona)
            }
            else {
                Log.i("GAAAAAAAAAAAAAA", riesgoArray[markList.indexOf(it)].toString())
                Log.i("GAAAAAAAAAAAAAA", markList.indexOf(it).toString())
                val zona = ZonaRiesgoBD(markList.indexOf(it),it.title, it.position.latitude.toString(),it.position.longitude.toString(),riesgoArray[markList.indexOf(it)])
                db.insertData(zona)
            }
        }

        googleMap.setMaxZoomPreference(18f)
        googleMap.setMinZoomPreference(10.5f)
        googleMap.setOnCameraMoveListener {
            resizeMarks(googleMap)
            if (!hidrantesMostrados) { mostrarHidrantes(googleMap) }
            else { ocultarHidrantes(googleMap)
                }
        }
        val Lima = LatLng(-12.0554671, -77.0431111)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Lima, 11.0f))

        googleMap.setOnMarkerClickListener(object :GoogleMap.OnMarkerClickListener{
            override fun onMarkerClick(p0: Marker?): Boolean {
                if (p0 in recursosMarkerArray) {
                    Log.i("Marcador","Hidrante")
                    return true
                }
                else
                {
                    Log.i("Marcador","No es")
                    return false
                }
            }
        })

        googleMap.setOnMyLocationButtonClickListener {
            val locatioManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locatioManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(activity, "El GPS no esta activado",Toast.LENGTH_SHORT).show()
                false
            }
            else{
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mylocation,16.0f))
                true
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        mFusedLocationClient = FusedLocationProviderClient(activity!!)
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_maps,container,false)

        db = DatabaseHandler(context!!)
        imgBtn_recargar = view.findViewById(R.id.imgBtn_recargar)
        prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        com.google.android.libraries.places.api.Places.initialize(context!!, context!!.resources.getString(R.string.google_maps_key) )
        token = prefs.getString("token","")
        listMarcadores = arrayListOf()
        markList = arrayListOf()
        zonasRiesgoList = arrayListOf()
        riesgoArray = arrayListOf()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

        traerUbicacion()
        mapFragment?.getMapAsync(callback)
        Buscador(mapFragment!!)
        Traer_Marcadores(token!!,mapFragment)
        Traer_Hidrantes()
        imgBtn_recargar.setOnClickListener {
            recargar(mapFragment)
        }

    }

    @SuppressLint("MissingPermission")
    private fun traerUbicacion(){

        locationRequest = LocationRequest.create()
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setInterval(20 * 1000)
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                    if (locationResult == null) {
                        return;
                    }
                    for (location in locationResult.getLocations()) {
                        if (location != null) {
                            mylocation = LatLng(location.latitude, location.longitude)
                        }
                }
                super.onLocationResult(locationResult)
            }
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }


    private fun Traer_Marcadores(token:String,mapFragment: SupportMapFragment): List<Marker>{
       zonasRiesgoList.clear()
       listMarcadores.clear()
       riesgoArray.clear()
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

                var zonacolor = 0
                if (zonas == null)return

                for (item in zonas!!.data){
                    zonasRiesgoList.add(item)
                    when(item.type!!.idtipo)
                    {
                        1 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_foreground) as BitmapDrawable; zonacolor = 1 }
                        2 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_medio_foreground) as BitmapDrawable; zonacolor = 2}
                        3 -> { bitmapdraw = resources.getDrawable(R.mipmap.ic_riesgo_bajo_foreground) as BitmapDrawable; zonacolor = 3 }
                        else -> { }
                    }
                    bitmap = bitmapdraw!!.bitmap

                    var relativePixelSize : Long = 100
                    if(zoomGlobal != 0f)  relativePixelSize = Math.round(2*Math.pow(1.4, zoomGlobal.toDouble()))

                    smallMaker= Bitmap.createScaledBitmap(bitmap,relativePixelSize.toInt(),relativePixelSize.toInt(),false)

                    val marker = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMaker))
                        .position(LatLng(item.latitude!!,item.longitude!!)).title(item.address).anchor(0.5f,0.5f).flat(false)
                    if(item.name != "") {
                        marker.snippet(item.name)
                    }
                    listMarcadores.add(marker)
                    riesgoArray.add(zonacolor)
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


    private fun Traer_Hidrantes() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        var bitmapdraw : BitmapDrawable? = null
        var bitmap:Bitmap
        var smallMaker :Bitmap
        bitmapdraw = resources.getDrawable(R.drawable.hidrante) as BitmapDrawable
        bitmap = bitmapdraw!!.bitmap

        smallMaker= Bitmap.createScaledBitmap(bitmap,50, 50,false)

        val recursosApiService = retrofit.create(RecursosApiService::class.java)


        recursosApiService.getHidrantes(token!!).enqueue(object : Callback<RecursosDTO> {
            override fun onResponse(call: Call<RecursosDTO>, response: Response<RecursosDTO>) {
                val hidrantes = response.body()!!.data
                Log.i("Hidrantes", hidrantes.toString())

                for (item in hidrantes)
                {
                    val marker = MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(smallMaker)).position(LatLng(item.coordenateY!!,item.coordenateX!!))
                        .title(item.locdes).anchor(0.5f,0.5f).flat(false).draggable(false)
                    recursosArray.add(marker)
                }
            }
            override fun onFailure(call: Call<RecursosDTO>, t: Throwable) {
                Log.i("AAAA", "MAaaaal")
            }
        })
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
                    /*googleMap.setOnMarkerClickListener(object :GoogleMap.OnMarkerClickListener{
                        override fun onMarkerClick(p0: Marker?): Boolean {
                            if (p0 == last || p0 == markerPer)
                            {
                                return false
                            }
                            return false
                        }
                    })*/
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.latLng,13f))


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

        Log.i("Zoom",zoom.toString())
        //Toast.makeText(activity, googleMap.cameraPosition.zoom.toString(), Toast.LENGTH_SHORT).show()

        var relativePixelSize = Math.round(pixelSizeAtZoom0*Math.pow(1.4,zoom.toDouble()));
        /*if (relativePixelSize > maxPixelSize)
        {
            //relativePixelSize = maxPixelSize.toLong()
            //Log.i("RelativePixesSizeNuevo",relativePixelSize.toString())
        }
*/

        if (abs(zoomGlobal - zoom) >= 1.0 )
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

    private fun hallaDistancia(lat1: Double,lat2: Double,lng1: Double,lng2 : Double): Double{
        val R = 6371;
        val dLat = deg2rad(lat2-lat1)
        val dLng = deg2rad(lng2-lng1)
        val a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                Math.sin(dLng/2) * Math.sin(dLng/2)
        ;
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        val d = R * c; // Distance in km
        return d;
    }

    private fun deg2rad(deg: Double): Double{
        return deg * (Math .PI/180)
    }


    fun mostrarHidrantes(googleMap: GoogleMap)
    {
            if(googleMap.cameraPosition.zoom >= 17) {
                Log.i("Estado", "Mostrando Hidrantes")
                val camLat= googleMap.cameraPosition.target.latitude
                val camLng= googleMap.cameraPosition.target.longitude

                for (item in recursosArray) {

                    if(hallaDistancia(camLat,item.position.latitude,camLng,item.position.longitude)< 1)
                    {
                        recursosMarkerArray.add(googleMap.addMarker(item))
                    }
                }
                hidrantesMostrados = true
            }
    }

    fun ocultarHidrantes(googleMap: GoogleMap)
    {

            if(googleMap.cameraPosition.zoom < 16.5 )
            {
                for (item in recursosMarkerArray)
                {
                    Log.i("Estado","Ocultando Hidrantes")
                    item.remove()
                }
                recursosMarkerArray.clear()
                hidrantesMostrados = false
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
        val latlng = LatLng(prefs.getString("latZonaRiesgo","")!!.toDouble(),prefs.getString("lngZonaRiesgo","")!!.toDouble())



        val exist = listMarcadores.any{markerOptions ->
            markerOptions.position == latlng
        }
        if (!exist)
        {
            Toast.makeText(context, "La zona de riesgo que ha seleccionado ya no existe", Toast.LENGTH_SHORT).show()
            return
        }


        var markerAux = markList.find {marker ->
            marker.position == latlng
        }

        Log.i("IDMARKER", idmarker.toString())

        mapFragment?.getMapAsync({ googleMap ->
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerAux!!.position,15f))
            markList[markList.indexOf(markerAux)].showInfoWindow()

        })
    }

    fun moverPer(){
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val lat = prefs.getString("lat","")!!.toDouble()
        val lng = prefs.getString("lng","")!!.toDouble()
        val add = prefs.getString("add","")
        //Log.i("IDMARKER", idmarker.toString())
        mapFragment?.getMapAsync { googleMap ->
            markerZonaPer = MarkerOptions().position(LatLng(lat,lng)).title(add).anchor(0.5f,0.5f).flat(false)
            if (markerPer!= null) markerPer!!.remove(); markerPer = null
            markerPer = googleMap.addMarker(markerZonaPer)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,lng),15f))
            googleMap.setOnMarkerClickListener(object :GoogleMap.OnMarkerClickListener{
                override fun onMarkerClick(p0: Marker?): Boolean {
                    if (p0 == markerPer) {
                        markerPer!!.remove()
                        markerPer = null
                        return false
                    }
                    return true
                }
            })
        }
    }
}


