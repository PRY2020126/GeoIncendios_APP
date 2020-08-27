package com.example.geoincendios.models

import java.io.Serializable

class Role(
    var idrol: Int,
    var role: String
) :Serializable{
    override fun toString(): String {
        return "Role(idrol=$idrol, role='$role')"
    }
}
