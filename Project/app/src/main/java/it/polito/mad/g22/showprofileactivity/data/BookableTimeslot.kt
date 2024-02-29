package it.polito.mad.g22.showprofileactivity.data

import kotlinx.serialization.Serializable

data class FirebaseUser(
    var bio: String = "",
    var birthDate: String = "",
    var email: String = "",
    var fullname:String = "",
    var image:String = "android.resource://it.polito.mad.g22.showprofileactivity/drawable/avatar",
    var nickname:String = "",
    var tel:String = "",
    var booking: List<FirebaseUserBooking> = emptyList(),
    var evaluation: List<FirebaseSportEvaluation> = emptyList(),
    var languages: List<String> = emptyList()
)
fun FirebaseUser.withEmail(email:String): FirebaseUser{
    this.email = email
    return this
}

@Serializable
data class FirebaseUserBooking(
    val field:FirebaseSportField = FirebaseSportField(),
    val booking_owner: Boolean = false,
    var start_at: String = "",
    var ends_at: String = "",
    var review: Int? = null
)


@Serializable
data class BookableTimeslot(
    //var key: String = "",
    val start_at: String = "",
    val ends_at: String = "",
    var field: Map<String, TimeSlotField> = emptyMap(),
    //var bookedByMe: Boolean = false,
    //var otherSportBooked: Boolean = false
)

/*
fun BookableTimeslot.withKey(key: String): BookableTimeslot {
    this.key = key
    return this
}

fun BookableTimeslot.withSport(sport: String): BookableTimeslot {
    this.otherSportBooked = this.field.filter {
        it.value.booked_by == "abc@def.it" && it.value.sport.name != sport
    }.isNotEmpty()
    this.field = this.field.filter { it.value.sport.name == sport && (it.value.booked_by == null || it.value.booked_by == "abc@def.it") }
    this.bookedByMe = this.field.filter { it.value.booked_by == "abc@def.it" }.isNotEmpty()
    return this
}

 */

@Serializable
data class TimeSlotField(
    val address: String = "",
    var booked_by: String? = "",
    val sport: TimeSlotSport = TimeSlotSport(),
    var requested_players: Int? = null,
    var joined_players: List<String> = emptyList()
)

@Serializable
data class TimeSlotSport(
    val name: String = "",
    val players: Int = 0,
    var drawable: String =""
)

data class FirebaseSport(
    val players: Int = 0,
    val drawable: String =""
)

data class FirebaseSportEvaluation(
    val sport_name: String = "",
    val sport_drawable: String = "",
    var stars: Int = 0
)

@Serializable
data class FirebaseSportField(
    val address: String = "",
    val name: String = "",
    val sport: TimeSlotSport = TimeSlotSport(),
    var options: Map<String, FirebaseOption> = emptyMap(),
)

@Serializable
data class FirebaseOption(
    val description: String = "",
    val price: Float = 0.0f
)