package com.example.geoincendios.models

import java.io.Serializable

class Recurso(
    var id : Int?,
    var operable : Int?,
    var locdes: String?,
    var distric: Int,
    var coordenateX : Double,
    var coordenateY: Double,
    var resourceType : TipoRecurso
) :Serializable {
    override fun toString(): String {
        return "Recurso(id=$id, operable=$operable, locdes=$locdes, distric=$distric, coordenateX=$coordenateX, coordenateY=$coordenateY, resourceType=$resourceType)"
    }
}