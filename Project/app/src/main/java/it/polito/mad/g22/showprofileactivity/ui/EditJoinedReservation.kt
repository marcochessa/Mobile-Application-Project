package it.polito.mad.g22.showprofileactivity.ui

import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
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

class EditJoinedReservationViewModel(application: Application) : AndroidViewModel(application){
    val firebaseDb = Firebase.firestore
    lateinit var booking: FirebaseUserBooking
    val userEmail = SavedPreference.getEmail(application)!!

    var selectedOptions: MutableLiveData<Map<String, FirebaseOption>> = MutableLiveData<Map<String, FirebaseOption>>()
    fun getOptions(){
        firebaseDb.collection("user")
            .document(userEmail)
            .addSnapshotListener{ user, error->
                val fields = user!!.toObject(FirebaseUser::class.java)!!.booking.filter {
                    it.start_at == booking.start_at && it.ends_at == booking.ends_at
                }.map { bk -> bk.field.options }
                if(fields.isNotEmpty())
                    selectedOptions.value = fields[0]
            }
    }

    fun deleteBooking(navController: NavController) {
        firebaseDb.collection("/user")
            .document("/${userEmail}")
            .get()
            .addOnSuccessListener {
                val modifiedUser = it.toObject(FirebaseUser::class.java)
                if (modifiedUser != null) {
                    modifiedUser.booking = modifiedUser.booking.filter {
                        it.start_at != booking.start_at && it.ends_at != booking.ends_at
                    }
                    firebaseDb.collection("/user")
                        .document("/${userEmail}")
                        .set(modifiedUser)
                }

                firebaseDb.collection("/timeslot")
                    .document("/${booking.start_at} ${booking.ends_at}")
                    .get()
                    .addOnSuccessListener {
                        val modifiedTimeslot = it.toObject(BookableTimeslot::class.java)
                        if (modifiedTimeslot != null) {
                            modifiedTimeslot.field[booking.field.name]!!.joined_players =
                                modifiedTimeslot.field[booking.field.name]!!.joined_players.filter { it!=userEmail }
                            firebaseDb.collection("/timeslot")
                                .document("/${booking.start_at} ${booking.ends_at}")
                                .set(modifiedTimeslot)
                                .addOnSuccessListener {
                                    navController.popBackStack()
                                }
                        }
                    }
            }
    }
}

class EditJoinedReservationFragment : Fragment(R.layout.edit_joined_reservation){
    val viewModel by viewModels<EditJoinedReservationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.booking = Json.decodeFromString(arguments?.getString("myreservation")!!)
        viewModel.getOptions()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sportTextView = requireView().findViewById<TextView>(R.id.textView)
        val fieldTextView = requireView().findViewById<TextView>(R.id.textView3)
        val dateTextView = requireView().findViewById<TextView>(R.id.textView9)
        sportTextView.text = viewModel.booking.field.sport.name
        fieldTextView.text = viewModel.booking.field.name
        val startAt = LocalDateTime.parse(viewModel.booking.start_at).format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm"))
        val endAt = LocalDateTime.parse(viewModel.booking.ends_at).format(DateTimeFormatter.ofPattern("hh:mm"))
        dateTextView.text = "${startAt} - ${endAt}"
        val deleteButton = requireView().findViewById<Button>(R.id.btn_elimina)

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

        deleteButton.setOnClickListener {
            viewModel.selectedOptions.removeObservers(viewLifecycleOwner)
            deleteButton.text = "Loading..."
            viewModel.deleteBooking(findNavController())
        }
    }
}