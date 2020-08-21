package com.example.geoincendios.models

import java.io.Serializable
import java.sql.Date

class Usuario (
    var idusuario: String,
    var nombre: String,
    var apellido: String,
    var correo: String,
    var usuario: String,
    var clave: String,
    var rol: String,
    val estatus: String,
    val user_reg:String,
    val fec_reg:String,
    val cpc_reg:String,
    val user_mod:String,
    val cpc_mod:String,
    val fec_mod:String



): Serializable{

    /*override fun toString(): String {
        return "Usuario(user='$nombre', image='$apellido', rol='$idusuario')"
    }*/
}