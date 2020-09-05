package com.example.geoincendios.models



class Email (
    var email: String = "",
    var content: String = "",
    var subject: String = ""
) {
    override fun toString(): String {
        return "Email(email='$email', content='$content', subject='$subject')"
    }
}