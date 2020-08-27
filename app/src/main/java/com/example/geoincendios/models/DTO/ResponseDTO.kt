package com.example.geoincendios.models.DTO

import java.io.Serializable

class ResponseDTO(
    var errorCode: String,
    var errorMessage: String,
    var httpCode: String,
    var data: Object
) : Serializable {
}