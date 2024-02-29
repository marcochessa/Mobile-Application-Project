package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query

data class FreeFieldTimeSlot(
    @Embedded(prefix = "timeslot_")
    val t: Timeslot,
    @Embedded(prefix = "field_")
    val f: SportField,
    val flagBookedByMe: Boolean,
    val flagBookedByOther: Boolean,
    val id_sport: Int,
    val userHasSport: Boolean
)

@Dao
interface FreeFieldTimeSlotDao {
    @Query(
        "SELECT timeslot.id as 'timeslot_id', timeslot.starts_at as 'timeslot_starts_at', timeslot.ends_at as 'timeslot_ends_at', sport_field.id as 'field_id', sport_field.name as 'field_name', sport_field.address as 'field_address', sport_field.sport as 'field_sport', CASE WHEN booking.user IS :userEmail then true else false end as 'flagBookedByMe', CASE WHEN booking.user IS NOT :userEmail AND booking.user IS NOT null then true else false end as 'flagBookedByOther', sport2.id as 'id_sport', CASE WHEN (SELECT COUNT(*) FROM booking, sport_field, sport WHERE booking.field = sport_field.id AND sport_field.sport = sport.id AND booking.timeslot = timeslot.id AND booking.user = :userEmail ) > 0 then true else false end as 'userHasSport' " +
                "FROM booking, sport_field, timeslot, sport sport2 " +
                "WHERE booking.field == sport_field.id AND booking.timeslot = timeslot.id AND sport2.id = sport_field.sport AND strftime('%s', starts_at) BETWEEN strftime('%s', date('now')) AND strftime('%s', date('now', '+7 days')) AND sport_field.name= :nameField "
    )
    fun getFreeFieldTimeSlot(nameField: String, userEmail: String): LiveData<List<FreeFieldTimeSlot>>
}

//timeslot dato un campo sportivo