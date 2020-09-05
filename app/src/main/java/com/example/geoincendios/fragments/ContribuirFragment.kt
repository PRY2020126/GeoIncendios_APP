package com.example.geoincendios.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import com.example.geoincendios.R
import com.example.geoincendios.activities.MainActivity
import com.example.geoincendios.interfaces.UsuarioApiService
import com.example.geoincendios.interfaces.ZonaContribuidaApiService
import com.example.geoincendios.models.DTO.UserDTO
import com.example.geoincendios.models.Reason
import com.example.geoincendios.models.ZonaContribuida
import com.example.geoincendios.util.URL_API
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*


class ContribuirFragment : Fragment() {

    private var idreason = 0

    private var token = ""

    private lateinit var spn_motivos : Spinner

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null
    private lateinit var geocoder: Geocoder
    private lateinit var places: Places
    private lateinit var autocomplete: Autocomplete
    private lateinit var zonaContribuidaService: ZonaContribuidaApiService

    private lateinit var et_descripcion: EditText
    private lateinit var et_direccion: EditText
    private lateinit var btn_enviar: Button

    private  var punto : Marker? = null
    private  var latLng : LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        googleMap.setOnMapClickListener {
            latLng = LatLng(it.latitude, it.longitude)
            var addres :String ? = null

            if (!geocoder.getFromLocation(it.latitude,it.longitude,1).isEmpty())
            {
                addres = geocoder.getFromLocation(it.latitude,it.longitude,1).get(0)!!.getAddressLine(0)!!.toString()
                et_direccion.setText(addres)
            }

            else et_direccion.setText("Por favor marque un punto válido")
            //Toast.makeText(context,addres,Toast.LENGTH_SHORT).show()

            if(punto == null){
                punto = googleMap.addMarker(MarkerOptions().position(it!!).title(addres).draggable(true))
            }
            else
            {
                punto!!.position = it
            }
        }

        googleMap.setOnMarkerDragListener(object :GoogleMap.OnMarkerDragListener{
            override fun onMarkerDragEnd(p0: Marker?) {
                var addres :String ? = null
                if (!geocoder.getFromLocation(p0!!.position.latitude,p0!!.position.longitude,1).isEmpty())
                {
                    addres = geocoder.getFromLocation(p0!!.position.latitude,p0!!.position.longitude,1).get(0)!!.getAddressLine(0)!!.toString()
                    et_direccion.setText(addres)
                }
                else et_direccion.setText("Por favor marque un punto válido")
            }

            override fun onMarkerDragStart(p0: Marker?) {
            }

            override fun onMarkerDrag(p0: Marker?) {
            }

        })



        //geocoder.getFromLocationName(et_descripcion.text.toString(),1)


        val Lima = LatLng(-12.0554671, -77.0431111)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Lima, 11.0f))
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contribuir, container, false)

        val prefs = activity!!.getSharedPreferences("user", Context.MODE_PRIVATE)
        token = prefs.getString("token","")!!

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(URL_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        geocoder = Geocoder(context, Locale.getDefault())
        punto = null
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        zonaContribuidaService = retrofit.create(ZonaContribuidaApiService::class.java)
        btn_enviar = view.findViewById(R.id.btn_enviar_zona_contribuida)
        et_descripcion = view.findViewById(R.id.et_descripcion_o_motivo_contribuir)
        et_direccion = view.findViewById(R.id.et_direccion_contribuir)
        spn_motivos =  view.findViewById(R.id.spn_motivos)


        btn_enviar.setOnClickListener {
            enviarZonaContribuida()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapContribuir) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        spn_motivos.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                //Toast.makeText(activity,position.toString() +" "+ spn_motivos.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show()
                idreason = position
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    fun enviarZonaContribuida(){

        if (idreason == 0){
            Toast.makeText(activity,"Por favor seleccione un motivo", Toast.LENGTH_SHORT).show()
            return
        }
        if(punto == null)
        {
            Toast.makeText(activity,"Por favor seleccione un punto", Toast.LENGTH_SHORT).show()
            return
        }


        var zonaContribuida = ZonaContribuida(idzona=0, address = et_direccion.text.toString(),description = et_descripcion.text.toString(),
            latitude =latLng!!.latitude.toString(),longitude = latLng!!.longitude.toString(),reason = Reason(idreason,""))

        //Toast.makeText(activity, zonaContribuida.toString(), Toast.LENGTH_LONG).show()
        //return

        zonaContribuidaService.saveZonaContribuida(token,zonaContribuida).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                val usuario = response.body()
                Log.i("AHH",response.body().toString())
                Toast.makeText(activity, "Se ha enviado su contribucion, gracias.",Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.i("AHHH", "MAaaaal")
            }
        })


    }



    companion object{
        @JvmStatic
        fun newInstance() = ContribuirFragment()
    }

}