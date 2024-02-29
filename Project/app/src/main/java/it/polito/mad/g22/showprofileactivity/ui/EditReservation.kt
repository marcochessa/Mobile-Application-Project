package it.polito.mad.g22.showprofileactivity.ui

import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import java.util.*

class EditReservationViewModel(application: Application) : AndroidViewModel(application) {
    val firebaseDb = Firebase.firestore
    lateinit var booking: FirebaseUserBooking
    val userEmail = SavedPreference.getEmail(application)!!
    lateinit var newDateSelected: LocalDateTime
    var newDateTimeslot: MutableLiveData<Map<String, BookableTimeslot>> =
        MutableLiveData<Map<String, BookableTimeslot>>()
    val operationFinished = MutableLiveData<Int>(0)
    var expectedOperations = 0
    var missingPlayersNumber = 0
    var actualMissingPlayersNumber:MutableLiveData<Int> = MutableLiveData<Int>(0)
    var options: MutableLiveData<Map<String, FirebaseOption>> = MutableLiveData<Map<String, FirebaseOption>>()
    var selectedOptions: MutableLiveData<Map<String, FirebaseOption>> = MutableLiveData<Map<String, FirebaseOption>>()

    fun getOptions(sportField: String){
        firebaseDb.collection("/field")
            .document(sportField)
            .get()
            .addOnSuccessListener {
                options.value = it.toObject(FirebaseSportField::class.java)?.options!!
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

    }
    fun getMissingPlayers() {
        firebaseDb.collection("/timeslot")
            .document("/${booking.start_at} ${booking.ends_at}")
            .get()
            .addOnSuccessListener {
                val selectedTimeslot = it.toObject(BookableTimeslot::class.java)
                missingPlayersNumber =
                    selectedTimeslot!!.field[booking.field.name]!!.requested_players ?: 0
                actualMissingPlayersNumber.value =
                    missingPlayersNumber - selectedTimeslot!!.field[booking.field.name]!!.joined_players.size
            }
    }

    fun newDateSelected(newDate: LocalDateTime) {
        newDateSelected = newDate
        firebaseDb.collection("/timeslot")
            .whereGreaterThan("start_at", newDateSelected.withHour(0).withMinute(0).toString())
            .whereLessThan("start_at", newDateSelected.withHour(23).withMinute(59).toString())
            .get()
            .addOnSuccessListener { documents ->
                val availableTimeslot = documents.map {
                    it.id to it.toObject(BookableTimeslot::class.java)
                }.toMap()
                    .filter { it.key > LocalDateTime.now().toString() }
                    .filter {
                        it.value.field.filter {
                            (it.value.booked_by == null && it.key == booking.field.name)
                        }.isNotEmpty()
                    }
                    .filter { !it.value.field.map { it.value.booked_by }.contains(userEmail) }
                newDateTimeslot.value = availableTimeslot
            }
            .addOnFailureListener {
                throw it
                //throw Exception("EditReservationViewModel newDate timeslot error")
            }
    }

    fun deleteReservation() {
        expectedOperations = 2
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
                    operationFinished.value = operationFinished.value!! + 1
                }
            }

        firebaseDb.collection("/timeslot")
            .document("/${booking.start_at} ${booking.ends_at}")
            .get()
            .addOnSuccessListener {
                val modifiedTimeslot = it.toObject(BookableTimeslot::class.java)
                if (modifiedTimeslot != null) {
                    modifiedTimeslot.field[booking.field.name]!!.booked_by = null
                    firebaseDb.collection("/timeslot")
                        .document("/${booking.start_at} ${booking.ends_at}")
                        .set(modifiedTimeslot)
                }
                operationFinished.value = operationFinished.value!! + 1
            }
    }


    fun saveWithoutNewTimeslot(newMissingPlayers: Int, optionToInsert: Map<String,FirebaseOption>){
        expectedOperations = 2
        firebaseDb.collection("/timeslot")
            .document("/${booking.start_at} ${booking.ends_at}")
            .get()
            .addOnSuccessListener {
                val modifiedTimeslot = it.toObject(BookableTimeslot::class.java)
                if (modifiedTimeslot != null) {
                    modifiedTimeslot.field[booking.field.name]!!.requested_players =
                        newMissingPlayers
                    firebaseDb.collection("/timeslot")
                        .document("/${booking.start_at} ${booking.ends_at}")
                        .set(modifiedTimeslot)
                }
                operationFinished.value = operationFinished.value!! + 1
            }

        firebaseDb.collection("/user")
            .document("/${userEmail}")
            .get()
            .addOnSuccessListener {
                val modifiedUser = it.toObject(FirebaseUser::class.java)
                if (modifiedUser != null) {
                    modifiedUser.booking.forEach {
                        if(it.start_at == booking.start_at && it.ends_at == booking.ends_at){
                            it.field.options = optionToInsert
                        }
                    }
                    firebaseDb.collection("/user")
                        .document("/${userEmail}")
                        .set(modifiedUser)
                    operationFinished.value = operationFinished.value!! + 1
                }
            }

    }


    fun saveTimeslotChanges(newTimeslot: String,newMissingPlayers: Int, optionToInsert: Map<String,FirebaseOption>){
        expectedOperations = 3
        val date = newTimeslot.split(' ')[0]
        val start_time = newTimeslot.split(' ')[1]
        val end_time = newTimeslot.split(' ')[3]
        val start_datetime = LocalDateTime.parse(
            "$date $start_time",
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        )
        val end_datetime =
            LocalDateTime.parse("$date $end_time", DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))

        firebaseDb.collection("/user")
            .document("/${userEmail}")
            .get()
            .addOnSuccessListener {
                val modifiedUser = it.toObject(FirebaseUser::class.java)
                if (modifiedUser != null) {
                    modifiedUser.booking.forEach {
                        if (it.start_at == booking.start_at && it.ends_at == booking.ends_at) {
                            it.start_at = start_datetime.toString()
                            it.ends_at = end_datetime.toString()
                            it.field.options = optionToInsert
                        }
                    }
                    firebaseDb.collection("/user")
                        .document("/${userEmail}")
                        .set(modifiedUser)
                    operationFinished.value = operationFinished.value!! + 1
                }
            }

        // new timeslot query
        firebaseDb.collection("/timeslot")
            .document("/${start_datetime} ${end_datetime}")
            .get()
            .addOnSuccessListener {
                val modifiedTimeslot = it.toObject(BookableTimeslot::class.java)
                if (modifiedTimeslot != null) {
                    modifiedTimeslot.field[booking.field.name]!!.booked_by = userEmail
                    modifiedTimeslot.field[booking.field.name]!!.requested_players =
                        newMissingPlayers
                    firebaseDb.collection("/timeslot")
                        .document("/${start_datetime} ${end_datetime}")
                        .set(modifiedTimeslot)
                }
                operationFinished.value = operationFinished.value!! + 1
            }

        // old timeslot query
        firebaseDb.collection("/timeslot")
            .document("/${booking.start_at} ${booking.ends_at}")
            .get()
            .addOnSuccessListener {
                val modifiedTimeslot = it.toObject(BookableTimeslot::class.java)
                if (modifiedTimeslot != null) {
                    modifiedTimeslot.field[booking.field.name]!!.booked_by = null
                    modifiedTimeslot.field[booking.field.name]!!.requested_players = 0
                    firebaseDb.collection("/timeslot")
                        .document("/${booking.start_at} ${booking.ends_at}")
                        .set(modifiedTimeslot)
                }
                operationFinished.value = operationFinished.value!! + 1
            }
    }
}

class EditReservationFragment : Fragment(R.layout.edit_delete_reservation) {
    private lateinit var optionDao: OptionDao
    private lateinit var userDao: UserDao
    private lateinit var bookingDao: BookingDao
    private lateinit var choosenOptionsDao: ChoosenoptionDao
    private lateinit var freeTimeSlotsForFieldDao: FreeTimeSlotsForFieldDao

    val viewModel: EditReservationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.booking =
            Json.decodeFromString<FirebaseUserBooking>(arguments?.getString("myreservation")!!)
        viewModel.getMissingPlayers()
        viewModel.getOptions(viewModel.booking.field.name)
        Log.d("Just debugging", "I'm debugging")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selected_sport = view.findViewById<TextView>(R.id.textView)
        val selected_playground = view.findViewById<TextView>(R.id.textView3)
        val datePickerButton = view.findViewById<ImageButton>(R.id.datePickerButton)
        val timeSlotSpinner = view.findViewById<Spinner>(R.id.timeSlotSpinner)
        val selected_datetime = view.findViewById<TextView>(R.id.textView20)
        val confirm_button = view.findViewById<Button>(R.id.btn_conferma)
        val delete_button = view.findViewById<Button>(R.id.btn_elimina)
        val cardView = view.findViewById<CardView>(R.id.cardview2)
        val startAtObject = LocalDateTime.parse(viewModel.booking.start_at)
        val endAtObject = LocalDateTime.parse(viewModel.booking.ends_at)
        val num_missing_players = view.findViewById<TextView>(R.id.textView5)
        val missingPlayersText = view.findViewById<TextView>(R.id.missingPlayersText)

        selected_sport.text = viewModel.booking.field.sport.name
        selected_playground.text = viewModel.booking.field.name
        viewModel.actualMissingPlayersNumber.observe(viewLifecycleOwner) {
            num_missing_players.text = viewModel.missingPlayersNumber.toString()
            missingPlayersText.text = viewModel.missingPlayersNumber.toString()
        }

        selected_datetime.text = "${
            startAtObject.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        } - ${endAtObject.format(DateTimeFormatter.ofPattern("HH:mm"))}"

        datePickerButton.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                // on below line we are passing context.
                requireContext(),
                { view, year, monthOfYear, dayOfMonth ->
                    // on below line we are setting
                    // date to our text view.
                    viewModel.newDateSelected(
                        LocalDateTime.of(
                            year,
                            monthOfYear + 1,
                            dayOfMonth,
                            0,
                            0
                        )
                    )
                },
                // on below line we are passing year, month
                // and day for the selected date in our date picker.
                year,
                month,
                day
            )
            // at last we are calling show
            // to display our date picker dialog.
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }

        viewModel.newDateTimeslot.observe(viewLifecycleOwner) {
            timeSlotSpinner.visibility = View.VISIBLE
            val timeslotAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                it.map {
                    "${
                        LocalDateTime.parse(it.value.start_at)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    } - " +
                            "${
                                LocalDateTime.parse(it.value.ends_at)
                                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                            }"
                }
            )
            timeSlotSpinner.adapter = timeslotAdapter
        }

        viewModel.options.observe(viewLifecycleOwner) {options->
            viewModel.selectedOptions.observe(viewLifecycleOwner) {selOptions->
                val linearLayout = requireView().findViewById<LinearLayout>(R.id.optionsscrollview)
                for (option in options) {
                    if (linearLayout.childCount < options.size) {
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
                        if (selOptions.contains(option.key)) {
                            checkBox.isChecked = true
                        }
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
        }

        confirm_button.setOnClickListener {
            if (checkUpdateMissingPlayers()==-1){
                return@setOnClickListener
            }
            confirm_button.text = "Loading..."
            confirm_button.isClickable = false
            delete_button.isClickable = false
            viewModel.selectedOptions.removeObservers(viewLifecycleOwner)

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

            if (timeSlotSpinner.visibility == View.VISIBLE){
                val changedMissingPlayers = requireView().findViewById<EditText>(R.id.missingPlayersText).text.toString()
                viewModel.saveTimeslotChanges(timeSlotSpinner.selectedItem.toString(),changedMissingPlayers.toInt(), optionToInsert)
            }
            else{
                val changedMissingPlayers = requireView().findViewById<EditText>(R.id.missingPlayersText).text.toString()
                viewModel.saveWithoutNewTimeslot(changedMissingPlayers.toInt(), optionToInsert)
            }

            viewModel.operationFinished.observe(viewLifecycleOwner) {
                if (it == viewModel.expectedOperations) {
                    findNavController().popBackStack()
                }
            }
        }

        delete_button.setOnClickListener {
            delete_button.text = "Loading..."
            confirm_button.isClickable = false
            delete_button.isClickable = false
            viewModel.selectedOptions.removeObservers(viewLifecycleOwner)
            viewModel.options.removeObservers(viewLifecycleOwner)

            viewModel.deleteReservation()
            viewModel.operationFinished.observe(viewLifecycleOwner) {
                if (it == viewModel.expectedOperations) {
                    findNavController().popBackStack()
                }
            }
        }

        /*
        val db = AppDatabase.getDatabase(requireContext())
        optionDao = db.optionDao()
        choosenOptionsDao = db.choosenOptionDao()
        userDao = db.userDao()
        bookingDao = db.bookingDao()
        freeTimeSlotsForFieldDao = db.freeTimeSlotsForFieldDao()
        var userId = userDao.getAll()[0].email
        val r = Json.decodeFromString<ConfirmReservationData>(arguments?.getString("myreservation")!!)
        val fieldId = r.sportField.id
        val timeSlotId = r.timeSlotId
        var newTimeSlotId = r.timeSlotId
        val options = optionDao.getOptionByFieldId(fieldId)
        val booking = Booking(timeSlotId, fieldId, userId)
        val choption = choosenOptionsDao.getChoosenOptionByTimeSlotAndField(timeSlotId, fieldId)
        lateinit var choptionList: List<Int>
        val selected_sport = view.findViewById<TextView>(R.id.textView)
        val selected_playground = view.findViewById<TextView>(R.id.textView3)
        val datePickerButton = view.findViewById<ImageButton>(R.id.datePickerButton)
        val timeSlotSpinner = view.findViewById<Spinner>(R.id.timeSlotSpinner)
        val selected_datetime = view.findViewById<TextView>(R.id.textView20)
        val confirm_button = view.findViewById<Button>(R.id.btn_conferma)
        val delete_button = view.findViewById<Button>(R.id.btn_elimina)
        val cardView = view.findViewById<CardView>(R.id.cardview2)
        val selectedDate = Calendar.getInstance()

        // Imposta il listener per la selezione dello Spinner
        timeSlotSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Ottieni il timeslot selezionato
                val selectedTimeSlot = parent.getItemAtPosition(position) as String
                newTimeSlotId = selectedTimeSlot.substringBefore(":").toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Questo metodo viene chiamato quando non viene selezionato alcun elemento
            }
        }

        datePickerButton.setOnClickListener{
            val datePicker = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener{_, year, monthOfYear, dayOfMonth ->
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)
                    selected_datetime.text = formattedDate
                    val dayTimeSlotsLiveData = freeTimeSlotsForFieldDao.getAvailableTimeslotsForFieldAndStartsWith(fieldId, formattedDate, userId)
                    dayTimeSlotsLiveData.observe(viewLifecycleOwner) { dayTimeSlots ->
                        val timeSlots = dayTimeSlots.map { x ->
                            val startTime = x.startsAt.substringAfterLast(" ").substring(0, 5)
                            val endTime = x.endsAt.substringAfterLast(" ").substring(0, 5)
                            "${x.timeslot}: $startTime - $endTime" }
                        val timeSlotAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeSlots)
                        timeSlotSpinner.adapter = timeSlotAdapter
                        timeSlotAdapter.notifyDataSetChanged() // Aggiunta del metodo notifyDataSetChanged()
                    }

                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, 6)
            datePicker.datePicker.maxDate = calendar.timeInMillis

            datePicker.show()
        }

        selected_sport.text = r.sportName
        selected_playground.text = r.sportField.name
        selected_datetime.text = "${r.startTime} - ${r.endTime}"

        choption.observe(viewLifecycleOwner) {
            choptionList = it.map { it.option }
            options.observe(viewLifecycleOwner) {
                val linearLayout = view.findViewById<LinearLayout>(R.id.optionsscrollview)
                for ((index, option) in it.withIndex()) {
                    val checkBox = CheckBox(requireContext())
                    checkBox.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    checkBox.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_900))
                    checkBox.text = "${option.description!!} ${option.price!!}€"
                    checkBox.id = option.id
                    if (choptionList.contains(option.id)) {
                        checkBox.isChecked = true
                    }
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
                choption.removeObservers(viewLifecycleOwner)
                options.removeObservers(viewLifecycleOwner)
                bookingDao.updateBookingOption(Booking(timeSlotId, fieldId, userId),
                    view.findViewById<LinearLayout>(R.id.optionsscrollview).children.filter { (it as CheckBox).isChecked }.map {it.id}.toList())
                if (timeSlotId != newTimeSlotId) {
                    bookingDao.updateBookingTimeslot(booking, newTimeSlotId)
                }

                Toast.makeText(requireContext(), "Reservation updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }

            delete_button.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete reservation")
                    .setMessage("Are you sure you want to delete this reservation?")
                    .setPositiveButton("Yes") { _, _ ->
                        choption.removeObservers(viewLifecycleOwner)
                        options.removeObservers(viewLifecycleOwner)
                        bookingDao.deleteBooking(Booking(timeSlotId, fieldId, userId))
                        Toast.makeText(requireContext(), "Reservation deleted", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

         */
    }

    fun checkUpdateMissingPlayers(): Int {
        val maxPlayerUpdate: Int? = viewModel.actualMissingPlayersNumber.value
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
                .toInt()) > maxPlayerUpdate!!
        ) {  //al posto di 10 mettere numero players di quello sport
            num.setError("You are looking for too many players!")  //al posto di 10 mettere numero players di quello sport
            return -1
        } else return num.text.toString().toInt()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val linearLayout = requireView().findViewById<LinearLayout>(R.id.optionsscrollview)
        val booleanList =
            Json.encodeToString(linearLayout.children.map { if ((it as CheckBox).isChecked) true else false }
                .toList())
        outState.putString(
            "optionValues",
            booleanList
        )

    }
}
