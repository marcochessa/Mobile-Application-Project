package it.polito.mad.g22.showprofileactivity

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val fullname: String,
    val nickname: String,
    val dateofbirth: String,
    val languages: Array<String>,
    var email: String,
    val tel: String,
    val bio: String,
    val imageSrc: String
    )

fun defaultProfile() : Profile{
    return Profile(
        "Hannibal Berry",
        "@bigBerry",
        "06/03/1998",
        arrayOf<String>("IT", "EN"),
        "abc@def.it",
        "34657463545",
        "I am an avid amateur sportsperson who loves the thrill of competition and always strives to better myself. Whether it's on the court, field, or in the gym, I pour my heart and soul into every game, training session, and workout.",
        "android.resource://it.polito.mad.g22.showprofileactivity/drawable/avatar"
    )
}