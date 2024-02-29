package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "sport_field", foreignKeys = [ForeignKey(entity = Sport::class, parentColumns = ["id"], childColumns = ["sport" ])])
@kotlinx.serialization.Serializable
data class SportField (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name:String,
    val address: String,
    val sport: Int
)

@Dao
interface SportFieldDao {
    @Query("SELECT * FROM sport_field")
    fun getAll(): LiveData<List<SportField>>

    @Query( "SELECT sport_field.* " +
            "FROM sport_field, sport " +
            "WHERE sport_field.sport = sport.id " +
            "AND sport.name = :nameSport")
    fun loadSportFields(nameSport:String): LiveData<List<SportField>>
}