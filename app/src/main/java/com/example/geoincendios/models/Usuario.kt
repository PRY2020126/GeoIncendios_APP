package com.example.geoincendios.models

import java.io.Serializable
import java.sql.Date

class Usuario (
    var idusuario: String,
    var firtsName: String,
    var lastName: String,
    var email: String,
    var username: String,
    var password: String,
    var role: Role,
    var status: Int,
    var user_reg:String?,
    val fec_reg:String?,
    var cpc_reg:String?,
    var user_mod:String?,
    var cpc_mod:String?,
    var fec_mod:String?

): Serializable{
    override fun toString(): String {
        return "Usuario(idusuario='$idusuario', firtsName='$firtsName', lastName='$lastName', email='$email', " +
                "username='$username', password='$password', role=$role, status=$status, user_reg=$user_reg, " +
                "fec_reg=$fec_reg, cpc_reg=$cpc_reg, user_mod=$user_mod, cpc_mod=$cpc_mod, fec_mod=$fec_mod)"
    }
}