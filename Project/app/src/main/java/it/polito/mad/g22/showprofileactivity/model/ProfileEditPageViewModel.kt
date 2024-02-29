package it.polito.mad.g22.showprofileactivity.model

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.data.FirebaseSport
import it.polito.mad.g22.showprofileactivity.data.FirebaseSportEvaluation
import it.polito.mad.g22.showprofileactivity.data.FirebaseUser
import it.polito.mad.g22.showprofileactivity.data.Language
import it.polito.mad.g22.showprofileactivity.data.withEmail
import it.polito.mad.g22.showprofileactivity.ui.storageRef
import kotlinx.coroutines.tasks.await

class ProfileEditPageViewModel(application: Application) : AndroidViewModel(application){
    val firebaseDb = Firebase.firestore
    var userLiveData: MutableLiveData<FirebaseUser> = MutableLiveData<FirebaseUser>()
    var evaluationLiveData: MutableLiveData<List<FirebaseSportEvaluation>> = MutableLiveData<List<FirebaseSportEvaluation>>()
    val userEmail = SavedPreference.getEmail(application)!!
    var sports: Map<String, FirebaseSport> = mapOf()
    val languages : List<String> = listOf("IT", "EN", "DE", "FR", "ES")
    var imageUri: Uri? = null
    var newUser: Boolean = false

    //loading management
    val loadedData = MutableLiveData<Int>(0)
    val expectedData = 2

    init {

        firebaseDb.collection("/sport")
            .get()
            .addOnSuccessListener { fields ->
                sports = fields.map { it.id to it.toObject(FirebaseSport::class.java) }.toMap()
                loadedData.value = loadedData.value!! + 1
            }

        firebaseDb.collection("user")
            .document(userEmail)
            .addSnapshotListener{ user, error->
                if (user!!.data==null){
                    userLiveData.value = FirebaseUser().withEmail(userEmail)
                    evaluationLiveData.value = userLiveData.value!!.evaluation
                    newUser = true
                    imageUri = userLiveData.value!!.image.toUri()
                }
                else{
                    userLiveData.value = user!!.toObject(FirebaseUser::class.java)
                    evaluationLiveData.value = userLiveData.value!!.evaluation
                    imageUri = userLiveData.value!!.image.toUri()
                }
                loadedData.value = loadedData.value!! + 1
            }
    }
    suspend fun updateUser(){
        userLiveData.value!!.evaluation = evaluationLiveData.value!!
        lateinit var firebaseUri: String
        if(userLiveData.value!!.image != imageUri.toString()) {
            storageRef.child("${userEmail}/profileImage").putFile(imageUri!!).await()
            firebaseUri= storageRef.child("${userEmail}/profileImage").downloadUrl.await().toString()
        }
        firebaseDb.collection("/user")
            .document(userEmail)
            .get()
            .addOnSuccessListener {
                if(it.data==null){
                    if(userLiveData.value!!.image != imageUri.toString()){
                        userLiveData.value!!.image = firebaseUri
                    }
                    firebaseDb.collection("/user")
                        .document(userEmail)
                        .set(userLiveData.value!!)
                }
                else{
                    var userToBeUpdated = it.toObject(FirebaseUser::class.java)
                    if (userToBeUpdated != null) {
                        userToBeUpdated = userLiveData.value
                        if(userToBeUpdated!!.image != imageUri.toString()){
                            userToBeUpdated!!.image = firebaseUri
                        }
                        firebaseDb.collection("/user")
                            .document(userEmail)
                            .set(userToBeUpdated)
                    }
                }

            }
    }
}