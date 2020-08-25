package com.example.geoincendios.api.model

class Login {
    lateinit var user: String
    lateinit var password: String

    fun Login( user:String, password:String){
        this.user = user
        this.password = password
    }
}