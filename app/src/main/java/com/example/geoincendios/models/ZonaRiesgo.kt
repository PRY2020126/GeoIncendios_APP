package com.example.geoincendios.models

class ZonaRiesgo(
    var idzona: Int? = null,
    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var name: String? = null,
    var type: Tipo? = null,
    var cpc_mod: String? = null,
    var cpc_reg: String? = null,
    var fec_mod: String? = null,
    var fec_reg: String? = null,
    var user_mod: String? = null,
    var user_reg: String? = null
) {
    override fun toString(): String {
        return "ZonaRiesgo(idzona=$idzona, address='$address', latitude='$latitude', longitude='$longitude', name='$name', type=$type, cpc_mod='$cpc_mod', cpc_reg='$cpc_reg', fec_mod='$fec_mod', fec_reg='$fec_reg', user_mod='$user_mod', user_reg='$user_reg')"
    }
}

