package it.polito.mad.g22.showprofileactivity.ui

import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.data.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConfirmJoinReservationViewModel(application: Application) : AndroidViewModel(application) {
    val firebaseDb = Firebase.firestore
    val userEmail = SavedPreference.getEmail(application)!!
    lateinit var joinableField: Pair<String, Map.Entry<String, TimeSlotField>>
    var selectedOptions: MutableLiveData<Map<String, FirebaseOption>> = MutableLiveData<Map<String, FirebaseOption>>()
    fun getOptions(sportField: String){
        firebaseDb.collection("/timeslot")
            .document(joinableField.first)
            .get()
            .addOnSuccessListener {
                val timeslot = it.toObject(BookableTimeslot::class.java)
                val owner = timeslot!!.field.filter {sf-> sf.key == sportField }.map {it.value.booked_by}[0]

                firebaseDb.collection("user")
                    .document(owner!!)
                    .addSnapshotListener{ user, error->
                        val fields = user!!.toObject(FirebaseUser::class.java)!!.booking.filter {
                            it.start_at == joinableField.first.split(' ')[0] && it.ends_at == joinableField.first.split(' ')[1]
                        }.map { bk -> bk.field.options }
                        if(fields.isNotEmpty())
                            selectedOptions.value = fields[0]
                    }
            }
    }
    fun joinToBooking(navController: NavController) {
        firebaseDb.collection("/timeslot")
            .document(joinableField.first)
            .get()
            .addOnSuccessListener {
                val timeslotTobeInserted = it.toObject(BookableTimeslot::class.java)
                if (timeslotTobeInserted != null) {
                    timeslotTobeInserted.field[joinableField.second.key]!!.joined_players =
                        timeslotTobeInserted.field[joinableField.second.key]!!.joined_players + userEmail
                    firebaseDb.collection("/timeslot")
                        .document(joinableField.first)
                        .set(timeslotTobeInserted)
                    firebaseDb.collection("/user")
                        .document(userEmail)
                        .get()
                        .addOnSuccessListener {
                            val userToBeUpdated = it.toObject(FirebaseUser::class.java)
                            val bookingToBeInserted = FirebaseUserBooking(
                                FirebaseSportField(
                                    joinableField.second.value.address,//selectedField.second.address,
                                    joinableField.second.key,//selectedField.first,
                                    joinableField.second.value.sport,//bookableTimeslot.field[selectedField.first]!!.sport
                                    selectedOptions.value!!
                                ),
                                false,
                                joinableField.first.split(' ')[0],
                                joinableField.first.split(' ')[1]
                            )
                            if (userToBeUpdated != null) {
                                userToBeUpdated.booking = userToBeUpdated.booking + bookingToBeInserted
                                firebaseDb.collection("/user")
                                    .document(userEmail)
                                    .set(userToBeUpdated)
                                navController.popBackStack()
                            }
                        }
                }
            }
    }
}

class ConfirmJoinReservation : Fragment(R.layout.confirm_join_reservation_fragment) {
    val viewModel by viewModels<ConfirmJoinReservationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.joinableField =
            Json.decodeFromString<Pair<String, Map.Entry<String, TimeSlotField>>>(
                arguments?.getString("joinreservation")!!
            )
        viewModel.getOptions(viewModel.joinableField.second.key)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireView().findViewById<TextView>(R.id.textView).text =
            viewModel.joinableField.second.value.sport.name
        requireView().findViewById<TextView>(R.id.textView3).text =
            viewModel.joinableField.second.key
        val startAt = LocalDateTime.parse(viewModel.joinableField.first.split(' ')[0]).format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm")
        )
        val endAt = LocalDateTime.parse(viewModel.joinableField.first.split(' ')[1]).format(
            DateTimeFormatter.ofPattern("hh:mm")
        )
        requireView().findViewById<TextView>(R.id.textView20).text = "${startAt} - ${endAt}"


        viewModel.selectedOptions.observe(viewLifecycleOwner) {options->
            val linearLayout = requireView().findViewById<LinearLayout>(R.id.optionsscrollview)
            if(options.isNotEmpty()) {
                if (linearLayout.childCount < options.size) {
                    for (option in options) {
                        val textView = TextView(requireContext())
                        textView.layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        textView.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.grey_900
                            )
                        )
                        textView.text = "✔ ${option.value.description!!} ${option.value.price!!}€"
                        linearLayout.addView(textView)
                    }
                }
            } else {
                if (linearLayout.childCount < 1) {
                    val textView = TextView(requireContext())
                    textView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    textView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grey_900
                        )
                    )
                    textView.text = "No additional options for this match"
                    linearLayout.addView(textView)
                }
            }
        }


        val confirmButton = requireView().findViewById<Button>(R.id.confirm_button)
        confirmButton.setOnClickListener {
            viewModel.selectedOptions.removeObservers(viewLifecycleOwner)
            confirmButton.text = "Loading..."
            confirmButton.isClickable = false
            viewModel.joinToBooking(findNavController())
        }
    }
}