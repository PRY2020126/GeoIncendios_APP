package com.example.geoincendios.models

class ZonaContribuida(

    var idzona: Int,
    var address :String,
    var description: String,
    var latitude:String,
    var longitude:String,
    var reason:Reason
) {
    override fun toString(): String {
        return "ZonaContribuida(idzona=$idzona, address='$address', description='$description', latitude='$latitude', longitude='$longitude', reason=$reason)"
    }
}