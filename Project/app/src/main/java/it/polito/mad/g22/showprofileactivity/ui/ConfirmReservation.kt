package it.polito.mad.g22.showprofileactivity.ui

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.data.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConfirmReservationFragmentViewModel(application: Application) : AndroidViewModel(application){
    lateinit var bookableTimeslot:BookableTimeslot
    lateinit var selectedField: Pair<String,TimeSlotField>
    val firebaseDb = Firebase.firestore
    val userEmail = SavedPreference.getEmail(application)!!
    var options: MutableLiveData<Map<String, FirebaseOption>> = MutableLiveData<Map<String, FirebaseOption>>()

    fun getOptions(sportField: String){
        firebaseDb.collection("/field")
            .document(sportField)
            .get()
            .addOnSuccessListener {
                options.value = it.toObject(FirebaseSportField::class.java)?.options!!
            }

    }

    fun confirmReservation(missingPlayers: Int, optionToInsert: Map<String,FirebaseOption>){
        //get bookableTimeslot
        firebaseDb.collection("/timeslot")
            .document("${bookableTimeslot.start_at} ${bookableTimeslot.ends_at}")
            .get()
            .addOnSuccessListener {
                val timeSlotToBeBooked = it.toObject(BookableTimeslot::class.java)
                //select the specific field
                val fieldToBeBooked = timeSlotToBeBooked!!.field.filter { it.key == selectedField.first }.values.first()
                //update email and request player
                fieldToBeBooked.booked_by = userEmail
                fieldToBeBooked.requested_players = missingPlayers
                firebaseDb.collection("/timeslot")
                    .document("${bookableTimeslot.start_at} ${bookableTimeslot.ends_at}")
                    .set(timeSlotToBeBooked)
            }

        firebaseDb.collection("/user")
            .document(userEmail)
            .get()
            .addOnSuccessListener {
                val userToBeUpdated = it.toObject(FirebaseUser::class.java)
                val bookingToBeInserted = FirebaseUserBooking(
                    FirebaseSportField(
                        selectedField.second.address,
                        selectedField.first,
                        bookableTimeslot.field[selectedField.first]!!.sport,
                        optionToInsert
                    ),
                    true,
                    bookableTimeslot.start_at,
                    bookableTimeslot.ends_at
                )
                if (userToBeUpdated != null) {
                    userToBeUpdated.booking = userToBeUpdated.booking + bookingToBeInserted
                    firebaseDb.collection("/user")
                        .document(userEmail)
                        .set(userToBeUpdated)
                }
            }
    }
}


class ConfirmReservationFragment : Fragment(R.layout.confirm_reservation) {
    val viewModel by viewModels<ConfirmReservationFragmentViewModel>()

    private lateinit var optionDao: OptionDao
    private lateinit var userDao: UserDao

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //giacomo's code
        viewModel.bookableTimeslot = Json.decodeFromString<BookableTimeslot>(arguments?.getString("reservation")!!)
        viewModel.selectedField = Json.decodeFromString(arguments?.getString("selectedField")!!)
        viewModel.getOptions(viewModel.selectedField.first)
        val selected_sport = requireView().findViewById<TextView>(R.id.textView)
        val selected_playground = requireView().findViewById<TextView>(R.id.textView3)
        val selected_datetime = requireView().findViewById<TextView>(R.id.textView20)
        val confirm_button = requireView().findViewById<Button>(R.id.confirm_button)
        val cardView = requireView().findViewById<CardView>(R.id.cardview2)

        selected_sport.text = viewModel.selectedField.second.sport.name
        selected_playground.text = viewModel.selectedField.first
        val startAt = LocalDateTime.parse(viewModel.bookableTimeslot.start_at).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        val endAt = LocalDateTime.parse(viewModel.bookableTimeslot.ends_at).format(DateTimeFormatter.ofPattern("HH:mm"))
        selected_datetime.text = "${startAt} - ${endAt}"

        viewModel.options.observe(viewLifecycleOwner) {
            val linearLayout = requireView().findViewById<LinearLayout>(R.id.optionsscrollview)
            for (option in it) {
                if (linearLayout.childCount < it.size) {
                    val checkBox = CheckBox(requireContext())
                    checkBox.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    checkBox.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grey_900
                        )
                    )
                    checkBox.text = "${option.value.description!!} ${option.value.price!!}€"
                    checkBox.id = option.key.toInt()
                    if (savedInstanceState != null) {
                        val optionValuesList: List<Boolean> =
                            Json.decodeFromString(savedInstanceState.getString("optionValues")!!)
                        checkBox.isChecked = optionValuesList[option.key.toInt() - 1]
                    }
                    linearLayout.addView(checkBox)
                }
            }

            if (linearLayout.childCount == 0) {
                cardView.setCardBackgroundColor(Color.TRANSPARENT)
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
            }
        }

        confirm_button.setOnClickListener {
            val missingPlayers = checkNumberMissingPlayers()  //se != -1, numero di giocatori mancanti
            if (missingPlayers != -1) {
                val chosenOptionList =
                    requireView().findViewById<LinearLayout>(R.id.optionsscrollview).children
                        .filter { (it as CheckBox).isChecked }
                        .map {it.id}
                        .toList()
                var optionToInsert: Map<String, FirebaseOption> = mapOf()
                viewModel.options.observe(viewLifecycleOwner) {
                    optionToInsert = it.filter {option-> chosenOptionList.contains(option.key.toInt())}
                }

                viewModel.options.removeObservers(viewLifecycleOwner)
                viewModel.confirmReservation(missingPlayers, optionToInsert)
                //bookingDao.confirmBooking(booking, chosenOptionList)
                findNavController().popBackStack()
            }
        }


        /*
        val db = AppDatabase.getDatabase(requireContext())
        val bookingDao = db.bookingDao()
        optionDao = db.optionDao()
        userDao = db.userDao()
        val userId = userDao.getAll()[0].email
        val r = Json.decodeFromString<ConfirmReservationData>(arguments?.getString("reservation")!!)
        val fieldId = r.sportField.id
        val timeSlotId = r.timeSlotId
        val options = optionDao.getOptionByFieldId(fieldId)
        val booking = Booking(timeSlotId, fieldId, userId)

        val selected_sport = requireView().findViewById<TextView>(R.id.textView)
        val selected_playground = requireView().findViewById<TextView>(R.id.textView3)
        val selected_datetime = requireView().findViewById<TextView>(R.id.textView20)
        val confirm_button = requireView().findViewById<Button>(R.id.confirm_button)
        val cardView = requireView().findViewById<CardView>(R.id.cardview2)

        selected_sport.text = r.sportName
        selected_playground.text = r.sportField.name
        selected_datetime.text = "${r.startTime} - ${r.endTime}"

        options.observe(viewLifecycleOwner) {
            val linearLayout = requireView().findViewById<LinearLayout>(R.id.optionsscrollview)
            for ((index, option) in it.withIndex()) {
                val checkBox = CheckBox(requireContext())
                checkBox.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                checkBox.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_900))
                checkBox.text = "${option.description!!} ${option.price!!}€"
                checkBox.id = option.id
                if (savedInstanceState != null) {
                    val optionValuesList: List<Boolean> =
                        Json.decodeFromString(savedInstanceState.getString("optionValues")!!)
                    checkBox.isChecked = optionValuesList[index]
                }
                linearLayout.addView(checkBox)
            }

            if (linearLayout.childCount == 0) {
                cardView.setCardBackgroundColor(Color.TRANSPARENT)
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
            }
        }

        confirm_button.setOnClickListener {
            val missingPlayers = checkNumberMissingPlayers()  //se != -1, numero di giocatori mancanti
            if (missingPlayers != -1) {
                val chosenOptionList =
                    requireView().findViewById<LinearLayout>(R.id.optionsscrollview).children
                        .filter { (it as CheckBox).isChecked }
                        .map { Choosenoption(timeSlotId, fieldId, it.id) }
                        .toList()
                viewModel.confirmReservation(missingPlayers)
                //bookingDao.confirmBooking(booking, chosenOptionList)
                findNavController().popBackStack()
            }
        }

         */
    }

    fun checkNumberMissingPlayers(): Int {
        val maxPlayer: Int = viewModel.selectedField.second.sport.players
        val num = requireView().findViewById<EditText>(R.id.missingPlayersText)
        if (num.text.isNullOrEmpty()) {
            num.setError("This field is empty!")
            return -1
        } else if (!num.text.isDigitsOnly()) {
            num.setError("This is not a number!")
            return -1
        } else if ((num.text.toString()
                .toInt()) < 0
        ) {  //al posto di 10 mettere numero players di quello sport
            num.setError("Makes no sense!")
            return -1
        } else if ((num.text.toString()
                .toInt()) >= maxPlayer
        ) {  //al posto di 10 mettere numero players di quello sport
            num.setError("Players per match: ${maxPlayer}")  //al posto di 10 mettere numero players di quello sport
            return -1
        } else return num.text.toString().toInt()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val chosenOptionList: List<Boolean> =
            view?.findViewById<LinearLayout>(R.id.optionsscrollview)?.children
                ?.map { (it as CheckBox).isChecked }
                ?.toList() ?: emptyList()
        outState.putString("optionValues", Json.encodeToString(chosenOptionList))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            val optionValuesList: List<Boolean> =
                Json.decodeFromString(savedInstanceState.getString("optionValues")!!)
            view?.findViewById<LinearLayout>(R.id.optionsscrollview)?.children?.forEachIndexed { index, view ->
                if (view is CheckBox) {
                    view.isChecked = optionValuesList[index]
                }
            }
        }
    }
}
