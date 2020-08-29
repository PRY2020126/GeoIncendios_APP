package com.example.geoincendios.models.DTO

import com.example.geoincendios.models.Usuario
import java.io.Serializable

class UserDTO(
    var errorCode: String,
    var errorMessage: String,
    var httpCode: String,
    var data: Usuario
) : Serializable {
    override fun toString(): String {
        return "UserDTO(errorCode='$errorCode', errorMessage='$errorMessage', httpCode='$httpCode', data=$data)"
    }
}