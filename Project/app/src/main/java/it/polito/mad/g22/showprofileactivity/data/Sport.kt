package it.polito.mad.g22.showprofileactivity.data

import androidx.lifecycle.LiveData

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "sport")
data class Sport (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val drawable: String
)

@Dao
interface SportDao {
    @Query("SELECT * FROM sport")
    fun getAll(): LiveData<List<Sport>>

    @Query("SELECT * FROM sport")
    fun getAllList(): List<Sport>
}