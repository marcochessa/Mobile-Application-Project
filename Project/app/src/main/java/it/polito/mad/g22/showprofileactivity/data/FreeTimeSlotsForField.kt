package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

data class BookingTimeslot(
    val timeslot: Int,
    val field: Int,
    val startsAt: String,
    val endsAt: String
)
@Dao
interface FreeTimeSlotsForFieldDao {
    @Query("SELECT timeslot.id as timeslot, booking.field as field, timeslot.starts_at as startsAt, timeslot.ends_at as endsAt FROM timeslot, booking WHERE timeslot.id = booking.timeslot AND booking.user is NULL AND booking.field = :field AND timeslot.starts_at LIKE :startsWith || '%' AND timeslot.id NOT IN ( SELECT timeslot FROM booking WHERE user = :userId)"+
    " AND strftime('%s', starts_at) > strftime('%s', 'now','localtime')")
    fun getAvailableTimeslotsForFieldAndStartsWith(field: Int, startsWith: String, userId : String): LiveData<List<BookingTimeslot>>

}




/*
@Dao
interface FreeTimeSlotsForFieldDao {
    @Query("SELECT timeslot, field, starts_at AS startsAt, ends_at AS endsAt FROM timeslot JOIN booking ON timeslot.id = booking.timeslot WHERE booking.user IS NULL AND booking.field = :field AND starts_at LIKE :startsWith || '%'")
    fun getAvailableTimeslotsForFieldAndStartsWith(field: Int, startsWith: String, userId : String): LiveData<List<BookingTimeslot>>
}


 */