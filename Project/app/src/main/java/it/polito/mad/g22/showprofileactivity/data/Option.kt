package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "option", foreignKeys = [ForeignKey(entity = SportField::class, parentColumns = ["id"], childColumns = ["field" ])])
data class Option(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "price") val price: Float?,
    @ColumnInfo(name = "field") val field: Int?
    )

@Dao
interface OptionDao {
    @Query("SELECT * FROM option")
    fun getAll(): LiveData<List<Option>>

    @Query("SELECT * FROM option WHERE field = :fieldId")
    fun getOptionByFieldId(fieldId:Int): LiveData<List<Option>>
}