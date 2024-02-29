package it.polito.mad.g22.showprofileactivity.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.data.FirebaseUser


class ReservationViewModel(application: Application) : AndroidViewModel(application) {
    val firebaseDb = Firebase.firestore
    var userLiveData: MutableLiveData<FirebaseUser> = MutableLiveData<FirebaseUser>()
    val userEmail = SavedPreference.getEmail(application)!!
    init {
        firebaseDb.collection("user")
            .document(userEmail)
            .addSnapshotListener { snapshot, e ->
                if(e!=null){
                    throw Exception("Problem with user query")
                }
                userLiveData.value = snapshot!!.toObject(FirebaseUser::class.java)
            }
    }
}