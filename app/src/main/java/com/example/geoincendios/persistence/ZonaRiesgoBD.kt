package com.example.geoincendios.persistence


class ZonaRiesgoBD (
    var id: Int,
    var addres : String,
    var lat : String,
    var lng: String,
    var riesgo: Int
) {
    override fun toString(): String {
        return "ZonaRiesgoBD(id=$id, addres='$addres', lat='$lat', lng='$lng', riesgo=$riesgo)"
    }
}