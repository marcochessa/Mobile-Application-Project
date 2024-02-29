package it.polito.mad.g22.showprofileactivity.model

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.Profile
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.data.AppDatabase
import it.polito.mad.g22.showprofileactivity.data.BookableTimeslot
import it.polito.mad.g22.showprofileactivity.data.FirebaseSport
import it.polito.mad.g22.showprofileactivity.data.FirebaseSportField
import it.polito.mad.g22.showprofileactivity.data.FreeFieldsForSlot
import it.polito.mad.g22.showprofileactivity.data.SportField
import it.polito.mad.g22.showprofileactivity.defaultProfile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class AddReservationViewModel(application: Application) : AndroidViewModel(application) {
    //giacomo's code
    val firebaseDb = Firebase.firestore
    val userEmail = SavedPreference.getEmail(application)!!
    var fieldMap: Map<String, FirebaseSportField> = mapOf()
    var timeSlotMap: MutableLiveData<Map<String, BookableTimeslot>> = MutableLiveData<Map<String, BookableTimeslot>>()
    var sportMap: Map<String, FirebaseSport> = mapOf()
    var loadedData = MutableLiveData<Int>(0)
    var expectedData = 3
    var gSelectedSport: MutableLiveData<String> = MutableLiveData<String>("")
    var gSelectedField: MutableLiveData<String> = MutableLiveData<String>("")
    var sportAndFieldMediator = MediatorLiveData<String>()
    var gSportFilteredTimeslot: MutableLiveData<Map<String, BookableTimeslot>> =
        MutableLiveData<Map<String, BookableTimeslot>>()
        get() {
            val retMap = timeSlotMap.value!!.map {
                var modifiedTimeslot = it.value.copy()
                modifiedTimeslot.field = it.value.field.filter {
                    (it.value.sport.name == gSelectedSport.value && it.value.booked_by == null) ||
                            (it.value.booked_by == userEmail) ||
                            it.value.joined_players.contains(userEmail)
                }
                it.key to modifiedTimeslot
            }.toMap()
            return MutableLiveData(retMap)
        }

    var gFieldFilteredTimeslot: MutableLiveData<Map<String, BookableTimeslot>> =
        MutableLiveData<Map<String, BookableTimeslot>>()
        get() {
            val retMap = timeSlotMap.value!!.map {
                var modifiedTimeslot = it.value.copy()
                modifiedTimeslot.field = it.value.field.filter {
                    (it.key == gSelectedField.value && it.value.booked_by == null) ||
                            it.value.booked_by == userEmail ||
                            it.value.joined_players.contains(userEmail)
                }
                it.key to modifiedTimeslot
            }.toMap()
            return MutableLiveData(retMap)
        }



    init {
        sportAndFieldMediator.addSource(gSelectedSport){
            value-> if (value.isNotEmpty()) sportAndFieldMediator.value = "Sport"
        }
        sportAndFieldMediator.addSource(gSelectedField){
                value-> if (value.isNotEmpty()) sportAndFieldMediator.value = "Field"
        }
        firebaseDb.collection("/field")
            .get()
            .addOnSuccessListener { fields ->
                fieldMap =
                    fields.map { it.id to it.toObject(FirebaseSportField::class.java) }.toMap()
                loadedData.value = loadedData.value!! + 1
            }

        firebaseDb.collection("/timeslot")
            .whereGreaterThan(
                "start_at",
                LocalDateTime.now().toString()
            )
            .whereLessThan(
                "start_at",
                LocalDateTime.now().plusDays(7).withHour(0).withMinute(0).toString()
            )
            .addSnapshotListener { timeslots, error ->
                timeSlotMap.value =
                    timeslots!!.map { it.id to it.toObject(BookableTimeslot::class.java) }.toMap()
                loadedData.value = loadedData.value!! + 1
            }


        firebaseDb.collection("/sport")
            .get()
            .addOnSuccessListener { fields ->
                sportMap = fields.map { it.id to it.toObject(FirebaseSport::class.java) }.toMap()
                loadedData.value = loadedData.value!! + 1
            }
    }

}