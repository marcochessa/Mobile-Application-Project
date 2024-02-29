package it.polito.mad.g22.showprofileactivity.ui

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.Profile
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.adapter.ReviewAdapter
import it.polito.mad.g22.showprofileactivity.data.*
import it.polito.mad.g22.showprofileactivity.defaultProfile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime


class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    fun updateReview(review: FirebaseUserBooking, rating: Float, dialog: Dialog) {
        firebaseDb.collection("/user")
            .document(userEmail)
            .get()
            .addOnSuccessListener {
                val userToBeModified = it.toObject(FirebaseUser::class.java)
                userToBeModified!!.booking.filter { it.start_at == review.start_at &&  it.ends_at == review.ends_at}.first().review = rating.toInt()
                firebaseDb.collection("/user")
                    .document(userEmail)
                    .set(userToBeModified)
                    .addOnSuccessListener {
                        dialog.dismiss()
                    }
            }
    }

    fun deleteReview(review: FirebaseUserBooking, dialog: Dialog) {
        firebaseDb.collection("/user")
            .document(userEmail)
            .get()
            .addOnSuccessListener {
                val userToBeModified = it.toObject(FirebaseUser::class.java)
                userToBeModified!!.booking.filter { it.start_at == review.start_at &&  it.ends_at == review.ends_at}.first().review = null
                firebaseDb.collection("/user")
                    .document(userEmail)
                    .set(userToBeModified)
                    .addOnSuccessListener {
                        dialog.dismiss()
                    }
            }
    }

    //giacomo's code
    val firebaseDb = Firebase.firestore
    val userEmail = SavedPreference.getEmail(application)!!
    val userBookings: MutableLiveData<List<FirebaseUserBooking>> =
        MutableLiveData<List<FirebaseUserBooking>>()

    init {
        firebaseDb.collection("/user")
            .document(userEmail)
            .addSnapshotListener { user, error ->
                val dbUser = user!!.toObject(FirebaseUser::class.java)
                userBookings.value = dbUser!!.booking.filter { it.ends_at < LocalDateTime.now().toString() }
            }
    }
}

class ReviewsFragment : Fragment(R.layout.fragment_reviews) {
    val viewModel: ReviewViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerPendingView: RecyclerView = view.findViewById(R.id.pendingReviewLayout)
        recyclerPendingView.layoutManager =
            LinearLayoutManager(requireContext())

        /*viewModel.checkforDataUpdate()

        val recyclerPendingView : RecyclerView = view.findViewById(R.id.pendingReviewLayout)
        recyclerPendingView.layoutManager =
            LinearLayoutManager(requireContext())
        viewModel.pendingReviews.observe(viewLifecycleOwner){reviews ->
            viewModel.sportFields.observe(viewLifecycleOwner){sportFields ->
                val adapter = ReviewAdapter(reviews, sportFields, requireContext(), viewModel)
                recyclerPendingView.adapter = adapter
            }
        }

         */
        viewModel.userBookings.observe(viewLifecycleOwner) {
            val adapter = ReviewAdapter(it,requireContext(), viewModel)
            recyclerPendingView.adapter = adapter
        }
    }
}