package it.polito.mad.g22.showprofileactivity.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timeslot")
data class Timeslot (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val starts_at: String,
    val ends_at: String
)