package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "booking", primaryKeys = ["timeslot", "field"], foreignKeys = [
    ForeignKey(entity = Timeslot::class, parentColumns = ["id"], childColumns = ["timeslot" ]),
    ForeignKey(entity = SportField::class, parentColumns = ["id"], childColumns = ["field" ]),
    ForeignKey(entity = User::class, parentColumns = ["email"], childColumns = ["user" ],onUpdate = ForeignKey.CASCADE)
])
data class Booking(
    val timeslot: Int,
    val field: Int,
    val user: String? = null
)

@Dao
abstract class BookingDao {
    @Query("SELECT * FROM booking")
    abstract fun getAll(): LiveData<List<Booking>>

    @Query("INSERT INTO choosen_option VALUES(:timeslot, :field, :option)")
    abstract fun insertOption(timeslot: Int, field: Int, option: Int)

    @Query("DELETE FROM choosen_option WHERE timeslot=:timeslot AND field = :field")
    abstract fun deletePreceedingOptions(timeslot: Int, field: Int)

    @Query("UPDATE booking SET user=:email WHERE timeslot = :timeslot AND field=:field")
    abstract fun updateBookingEmail(email: String, timeslot: Int, field:Int)

    @Query("UPDATE booking SET user = null WHERE timeslot = :timeslot AND field=:field")
    abstract fun updateBookingEmailToNull(timeslot: Int, field:Int)

    @Query("UPDATE choosen_option SET timeslot=:newTimeslot WHERE timeslot=:oldTimeslot AND field = :field")
    abstract fun updateTimeslotOptions(newTimeslot: Int, oldTimeslot: Int, field: Int)

    @Transaction
    open fun confirmBooking(booking: Booking, cOptions: List<Choosenoption>) {
        for (option in cOptions){
            insertOption(option.timeslot,option.field,option.option)
        }
        updateBookingEmail(booking.user!!, booking.timeslot, booking.field)
    }

    @Transaction
    open fun deleteBooking(booking:Booking){
        deletePreceedingOptions(booking.timeslot,booking.field)
        updateBookingEmailToNull(booking.timeslot,booking.field)
    }

    @Transaction
    open fun updateBookingTimeslot(booking: Booking, newTimeslot: Int){
        updateBookingEmailToNull(booking.timeslot, booking.field)
        updateBookingEmail(booking.user!!, newTimeslot,booking.field)
        updateTimeslotOptions(newTimeslot,booking.timeslot, booking.field)
    }

    @Transaction
    open fun updateBookingOption(booking:Booking, optionsIds: List<Int>){
        deletePreceedingOptions(booking.timeslot,booking.field)
        for (optionId in optionsIds){
            insertOption(booking.timeslot,booking.field,optionId)
        }
    }
}