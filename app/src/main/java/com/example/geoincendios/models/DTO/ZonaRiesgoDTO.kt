package com.example.geoincendios.models.DTO

import com.example.geoincendios.models.Usuario
import com.example.geoincendios.models.ZonaRiesgo

class ZonaRiesgoDTO (
    var errorCode: String,
    var errorMessage: String,
    var httpCode: String,
    var data: List<ZonaRiesgo>
) {
    override fun toString(): String {
        return "ZonaRiesgoDTO(errorCode='$errorCode', errorMessage='$errorMessage', httpCode='$httpCode', data=$data)"
    }
}