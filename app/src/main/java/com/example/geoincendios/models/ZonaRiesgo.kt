package com.example.geoincendios.models

class ZonaRiesgo(
    var idzona: Int,
    var address: String,
    var latitude: Double,
    var longitude: Double,
    var name: String,
    var type: Tipo,
    var cpc_mod: String,
    var cpc_reg: String,
    var fec_mod: String,
    var fec_reg: String,
    var user_mod: String,
    var user_reg: String
) {
    override fun toString(): String {
        return "ZonaRiesgo(idzona=$idzona, address='$address', latitude='$latitude', longitude='$longitude', name='$name', type=$type, cpc_mod='$cpc_mod', cpc_reg='$cpc_reg', fec_mod='$fec_mod', fec_reg='$fec_reg', user_mod='$user_mod', user_reg='$user_reg')"
    }
}

