package com.example.geoincendios.models.DTO

import java.io.Serializable

class LoginDTO (
    var username: String,
    var password: String
) : Serializable{
    override fun toString(): String {
        return "LoginDTO(username='$username', password='$password')"
    }
}