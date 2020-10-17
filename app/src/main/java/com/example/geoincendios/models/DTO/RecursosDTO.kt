package com.example.geoincendios.models.DTO

import com.example.geoincendios.models.Recurso
import com.example.geoincendios.models.Usuario

class RecursosDTO(
    var errorCode: String?,
    var errorMessage: String?,
    var httpCode: String?,
    var data: List<Recurso>
) {
    override fun toString(): String {
        return "RecursosDTO(errorCode=$errorCode, errorMessage=$errorMessage, httpCode=$httpCode, data=$data)"
    }
}