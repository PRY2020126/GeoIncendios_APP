package com.example.geoincendios.models

class Reason (
    var idmotivo: Int,
    var reason:String
) {
    override fun toString(): String {
        return "Reason(idmotivo=$idmotivo, reason='$reason')"
    }
}