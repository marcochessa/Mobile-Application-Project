package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query

data class FreeSportTimeSlot(
    @Embedded (prefix = "timeslot_")
    val t:Timeslot,
    @Embedded (prefix = "field_")
    val f:SportField,
    val flagBooked: Boolean,
    val bookedOtherSlot: Boolean
)

    @Dao
    interface FreeSportTimeSlotDao {
        @Query("SELECT DISTINCT(timeslot) as 'timeslot_id', timeslot.starts_at as 'timeslot_starts_at', timeslot.ends_at as 'timeslot_ends_at', null as 'field_id', null as 'field_name', sport_field.address as 'field_address', sport_field.sport as 'field_sport', CASE WHEN booking.user IS :userEmail then true else false end as 'flagBooked', " +
                "false as 'bookedOtherSlot' "+
                "FROM booking, sport_field, sport, timeslot " +
                "WHERE booking.field == sport_field.id AND sport_field.sport= sport.id AND booking.timeslot = timeslot.id AND user is not null AND strftime('%s', starts_at) BETWEEN strftime('%s', date('now')) AND strftime('%s', date('now', '+7 days')) AND sport.name= :sport " +
                "AND timeslot not in ( " +
                " SELECT timeslot " +
                " FROM booking, sport_field, sport" +
                " WHERE booking.field == sport_field.id AND sport_field.sport= sport.id AND strftime('%s', starts_at) BETWEEN strftime('%s', date('now')) AND strftime('%s', date('now', '+7 days')) AND  (user is null OR user = :userEmail)) " +
                "UNION " +
                "SELECT timeslot, timeslot.starts_at as 'timeslot_starts_at', timeslot.ends_at as 'timeslot_ends_at', field, sport_field.name,  sport_field.address as 'field_address', sport_field.sport as 'field_sport', CASE WHEN booking.user IS :userEmail then true else false end as 'flagBooked', " +
                "false as 'bookedOtherSlot' "+
                "FROM booking, sport_field, sport, timeslot " +
                "WHERE booking.field == sport_field.id AND sport_field.sport= sport.id AND  booking.timeslot = timeslot.id AND (user is null OR user = :userEmail) AND strftime('%s', starts_at) BETWEEN strftime('%s', date('now')) AND strftime('%s', date('now', '+7 days')) AND sport.name= :sport" +
                " AND timeslot.id NOT IN  (SELECT booking.timeslot FROM booking, sport_field, sport WHERE booking.field == sport_field.id AND sport_field.sport= sport.id AND booking.user = :userEmail AND sport.name != :sport ) " +
                "UNION "+
                "SELECT timeslot, timeslot.starts_at as 'timeslot_starts_at', timeslot.ends_at as 'timeslot_ends_at', field, sport_field.name,  sport_field.address as 'field_address', sport_field.sport as 'field_sport', false as 'flagBooked', " +
                "true as 'bookedOtherSlot' " +
                "FROM booking, sport_field, sport, timeslot " +
                "WHERE booking.field == sport_field.id AND sport_field.sport= sport.id AND  booking.timeslot = timeslot.id AND booking.user = :userEmail AND sport.name != :sport AND strftime('%s', starts_at) BETWEEN strftime('%s', date('now')) AND strftime('%s', date('now', '+7 days'));")
        fun getFreeSlot(sport : String, userEmail: String): LiveData<List<FreeSportTimeSlot>>
    }