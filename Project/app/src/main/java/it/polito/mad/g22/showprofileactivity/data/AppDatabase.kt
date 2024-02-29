package it.polito.mad.g22.showprofileactivity.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Booking::class, Choosenoption::class, Option::class, Sport::class, SportField::class, Review:: class, Timeslot::class, Language::class, UserLanguageLink::class, sportEvaluation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookedTimeslotDao(): BookedTimeslotDao
    abstract fun FreeFieldTimeSlotDao(): FreeFieldTimeSlotDao
    abstract fun userDao(): UserDao
    abstract fun bookingDao(): BookingDao
    abstract fun freeSportTimeSlotDao(): FreeSportTimeSlotDao
    abstract fun sportDao(): SportDao
    abstract fun optionDao(): OptionDao
    abstract fun choosenOptionDao() : ChoosenoptionDao
    abstract fun reviewDao(): ReviewDao
    abstract fun sportFieldDao(): SportFieldDao
    abstract fun freeTimeSlotsForFieldDao(): FreeTimeSlotsForFieldDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            (INSTANCE ?:
            synchronized(this) {
                val i = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,"Sample.db")
                    .createFromAsset("database/field_booking.db")
                    .allowMainThreadQueries().build()
                INSTANCE = i
                INSTANCE
            })!!
    }
}