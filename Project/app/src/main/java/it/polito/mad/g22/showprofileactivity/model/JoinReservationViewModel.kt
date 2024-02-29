package it.polito.mad.g22.showprofileactivity.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.data.BookableTimeslot
import java.time.LocalDateTime


class JoinReservationViewModel(application: Application) : AndroidViewModel(application) {
    val firebaseDb = Firebase.firestore
    var joinableTimeslot: MutableLiveData<List<BookableTimeslot>> = MutableLiveData<List<BookableTimeslot>>()
    val userEmail = SavedPreference.getEmail(application)!!

    init {

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
                joinableTimeslot.value =
                    timeslots!!.map { it.toObject(BookableTimeslot::class.java) }
                        .filter {
                            it.field.filter {
                                it.value.booked_by != null && it.value.booked_by != userEmail &&
                                        !it.value.joined_players.contains(userEmail) &&
                                        (it.value.joined_players.size < (it.value.requested_players ?: 0))
                            }.isNotEmpty()
                        }
                        .map {
                            val onlyJoinableFields = it
                            onlyJoinableFields.field =
                                onlyJoinableFields.field.filter {
                                    it.value.booked_by != null && it.value.booked_by != userEmail &&
                                            !it.value.joined_players.contains(userEmail)
                                            && (it.value.joined_players.size < it.value.requested_players!!)
                                }
                            onlyJoinableFields
                        }
            }

    }
}