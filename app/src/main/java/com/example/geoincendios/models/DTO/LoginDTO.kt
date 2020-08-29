package com.example.geoincendios.models.DTO

import java.io.Serializable

class LoginDTO (
    var email: String,
    var password: String
) : Serializable{
    override fun toString(): String {
        return "LoginDTO(email='$email', password='$password')"
    }

}