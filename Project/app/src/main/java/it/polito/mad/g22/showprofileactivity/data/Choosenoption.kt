package it.polito.mad.g22.showprofileactivity.data
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity (tableName = "choosen_option", primaryKeys = ["timeslot", "field", "option"], foreignKeys = [
ForeignKey(entity = Booking::class, parentColumns = ["timeslot", "field"], childColumns = ["timeslot", "field"]),
ForeignKey(entity = Option::class, parentColumns = ["id"], childColumns = ["option" ])
])
data class Choosenoption(
    val timeslot: Int,
    val field: Int,
    val option: Int
)

@Dao
interface ChoosenoptionDao {
    @Query("SELECT * FROM choosen_option")
    fun getAll(): List<Choosenoption>

    @Query("SELECT * FROM choosen_option WHERE timeslot=:timeSlot AND field = :field")
    fun getChoosenOptionByTimeSlotAndField(timeSlot: Int, field: Int): LiveData<List<Choosenoption>>
}