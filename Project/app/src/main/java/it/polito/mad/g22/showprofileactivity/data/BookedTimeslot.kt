package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query

data class BookedTimeslot (
    @Embedded val b: Booking,
    @Embedded val f: SportField,
    @Embedded(prefix = "sport_") val s: Sport,
    @Embedded(prefix = "timeslot_") val t: Timeslot
)

@Dao
interface BookedTimeslotDao {
    @Query(
        "SELECT booking.*, sport_field.*, sport.id as 'sport_id', sport.name as 'sport_name', sport.drawable as 'sport_drawable', " +
                "timeslot.id as 'timeslot_id', timeslot.starts_at as 'timeslot_starts_at', timeslot.ends_at as 'timeslot_ends_at'" +
                "FROM booking, sport_field, sport, timeslot " +
                "WHERE booking.field = sport_field.id " +
                "AND sport_field.sport = sport.id "+
                "AND booking.timeslot = timeslot.id "+
                "AND strftime('%s', starts_at) BETWEEN strftime('%s', date('now')) AND strftime('%s', date('now', '+7 days')) "+
                "AND booking.user = :userEmail"
    )
    fun loadBookedTimeslot(userEmail:String): LiveData<List<BookedTimeslot>>
}
