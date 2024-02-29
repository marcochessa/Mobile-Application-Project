package it.polito.mad.g22.showprofileactivity.adapter

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEntity
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.BookableTimeslot
import it.polito.mad.g22.showprofileactivity.data.SportField
import it.polito.mad.g22.showprofileactivity.model.AddReservationViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.*


@kotlinx.serialization.Serializable
data class ConfirmReservationData(
    val sportField: SportField,
    val sportName: String,
    val startTime: String,
    val endTime: String,
    val timeSlotId: Int
)


data class Reservation(
    var id: Long,
    val calTitle: String,
    val sportName: String,
    val drawableSport: String,
    val startTime: Calendar,
    val endTime: Calendar,
    val fields: MutableList<SportField>,
    val booked: Boolean,
    val bookedByOther: Boolean,
    val userHasSport: Boolean
)

class SportCalendarAdapter(
    private val navController: NavController,
    private val viewModel: AddReservationViewModel
) :
    WeekView.SimpleAdapter<BookableTimeslot>() {
    /*companion object {
        lateinit var navController: NavController

        fun setVar(t: NavController) {
            navController = t
        }
    }

     */

    override fun onCreateEntity(item: BookableTimeslot): WeekViewEntity {
        var style: WeekViewEntity.Style =
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.GREEN)
                .build() //= WeekViewEntity.Style.Builder().setBackgroundColor(Color.GRAY).build()

        if (item.field.filter { it.value.booked_by != null }
                .isNotEmpty() || item.field.isEmpty() || item.field.filter {
                it.value.joined_players.contains(
                    viewModel.userEmail
                )}.isNotEmpty()
        ) {
            style = WeekViewEntity.Style.Builder().setBackgroundColor(Color.GRAY).build()
        }

        /*
        var style: WeekViewEntity.Style = if (item.booked)
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.GREEN).build()
        else if (item.fields.size == 0 || item.bookedByOther || item.userHasSport)
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.RED).build()
        else
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.GRAY).build()

        if (item.startTime < Calendar.getInstance())
            style = WeekViewEntity.Style.Builder().setBackgroundColor(Color.RED).build()

         */
        val startLocalDateTime = LocalDateTime.parse(item.start_at)
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, startLocalDateTime.year)
            set(Calendar.MONTH, startLocalDateTime.month.value - 1)
            set(Calendar.DATE, startLocalDateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, startLocalDateTime.hour)
            set(Calendar.MINUTE, startLocalDateTime.minute)
            set(Calendar.SECOND, startLocalDateTime.second)
        }

        val endLocalDateTime = LocalDateTime.parse(item.ends_at)
        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, endLocalDateTime.year)
            set(Calendar.MONTH, endLocalDateTime.month.value - 1)
            set(Calendar.DATE, endLocalDateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, endLocalDateTime.hour)
            set(Calendar.MINUTE, endLocalDateTime.minute)
            set(Calendar.SECOND, endLocalDateTime.second)
        }
        return WeekViewEntity.Event.Builder(item)
            .setId(startCalendar.timeInMillis)//TODO renderlo dinamico
            .setTitle("")
            .setStartTime(startCalendar)
            .setEndTime(endCalendar)
            .setStyle(style)
            .build()
    }

    override fun onEventClick(data: BookableTimeslot) {
        if (data.field.filter { it.value.booked_by == viewModel.userEmail || it.value.joined_players.contains(viewModel.userEmail) }.isNotEmpty()) {
            val builder = AlertDialog.Builder(this.context)
            builder.setMessage("You already booked this timeslot!")
            builder.setNegativeButton("OK") { _, _ ->
            }
            builder.show()
            return
        }
        if (data.field.filter { it.value.booked_by == null }.isEmpty()) {
            val builder = AlertDialog.Builder(this.context)
            builder.setMessage("No fields available!")
            builder.setNegativeButton("OK") { _, _ ->
            }
            builder.show()
            return
        }
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle("Available Fields")
        val fieldsArray =
            data.field.filter { it.value.booked_by == null }.map { it.key }.toTypedArray()
        builder.setItems(fieldsArray) { _, which ->
            val bundle = Bundle()
            bundle.putString(
                "reservation",
                Json.encodeToString(data)
            )
            bundle.putString(
                "selectedField",
                Json.encodeToString(fieldsArray[which] to data.field[fieldsArray[which]])
            )
            navController.navigate(
                R.id.action_new_reservation_fragment_to_confirmReservationFragment2,
                bundle
            )
        }

        builder.setNegativeButton("Cancel") { _, _ ->
        }
        builder.create().show()
        /*
        val startTimeFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val endTimeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm")
        val strDate1: String = startTimeFormat.format(data.startTime.time)
        val strDate2: String = endTimeFormat.format(data.endTime.time)
        lateinit var confirmreservationdata: ConfirmReservationData

        if (data.startTime > Calendar.getInstance()) {
            if (!data.userHasSport && !data.booked) { //!data.userHasSport &&
                if (data.fields.size == 0 || data.bookedByOther) {
                    val builder = AlertDialog.Builder(this.context)

                    builder.setTitle("Error")
                    builder.setMessage("Timeslot not available")

                    builder.setNegativeButton("OK") { _, _ ->
                    }

                    builder.show()
                } else {
                    if (!data.calTitle.equals(data.sportName)) {
                        confirmreservationdata = ConfirmReservationData(
                            sportField = data.fields[0],
                            sportName = data.sportName,
                            startTime = strDate1,
                            endTime = strDate2,
                            timeSlotId = data.id.toInt()
                        )

                        val bundle = Bundle()
                        bundle.putString(
                            "reservation",
                            Json.encodeToString(confirmreservationdata)
                        )
                        navController.navigate(
                            R.id.action_new_reservation_fragment_to_confirmReservationFragment2,
                            bundle
                        )
                    } else {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("$strDate1 - $strDate2")

                        val bookableReservations = arrayOfNulls<String>(data.fields.size)
                        var i = 0
                        data.fields.forEach {
                            bookableReservations[i] = it.name
                            i++;
                        }

                        val checkedReservations = BooleanArray(data.fields.size) { false }

                        builder.setMultiChoiceItems(bookableReservations, checkedReservations,
                            OnMultiChoiceClickListener { dialog, which, isChecked ->
                                // The user checked or unchecked a box
                                checkedReservations[which] = isChecked
                                i = 0
                                for (i in 0..data.fields.size - 1) {
                                    if (i != which) {
                                        checkedReservations[i] = false
                                        (dialog as AlertDialog).listView.setItemChecked(
                                            i,
                                            false
                                        )
                                    }
                                }
                                confirmreservationdata = ConfirmReservationData(
                                    sportField = data.fields[which],
                                    sportName = data.sportName,
                                    startTime = strDate1,
                                    endTime = strDate2,
                                    timeSlotId = data.id.toInt()
                                )
                            })

                        builder.setPositiveButton("OK") { _, _ ->
                            if (checkedReservations.contains(true)) {
                                //val intent = Intent(context, ConfirmReservation::class.java)
                                //Pass any necessary data to ConfirmReservation activity
                                //intent.putExtra(
                                //    "reservation",
                                //    Json.encodeToString(confirmreservationdata)
                                //)
                                //context.startActivity(intent)

                                val bundle = Bundle()
                                bundle.putString(
                                    "reservation",
                                    Json.encodeToString(confirmreservationdata)
                                )
                                navController.navigate(
                                    R.id.action_new_reservation_fragment_to_confirmReservationFragment2,
                                    bundle
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Select a sport field",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                        builder.setNegativeButton("BACK", null)

                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            } else {
                //caso cal generale
                if (data.sportName.equals(data.calTitle)) {
                    if (data.booked) {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has already booked a field for this sport")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    }
                    if (data.userHasSport) {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has another reservation for this timeslot")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    }
                    //caso filtro sport
                } else {
                    if (data.booked) {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has already booked this field")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    } else {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has already booked another field")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    }
                }
            }
        } else {
            val builder = AlertDialog.Builder(this.context)

            builder.setTitle("Error")
            builder.setMessage("User cannot choose a past timeslot")

            builder.setNegativeButton("OK") { _, _ ->
            }

            builder.show()
        }
        */
    }
}

class FieldCalendarAdapter(
    private val navController: NavController,
    private val viewModel: AddReservationViewModel
) :
    WeekView.SimpleAdapter<BookableTimeslot>() {
    /*companion object {
        lateinit var navController: NavController

        fun setVar(t: NavController) {
            navController = t
        }
    }

     */

    override fun onCreateEntity(item: BookableTimeslot): WeekViewEntity {
        var style: WeekViewEntity.Style =
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.GREEN)
                .build() //= WeekViewEntity.Style.Builder().setBackgroundColor(Color.GRAY).build()

        if (item.field.filter { it.value.booked_by != null }.isNotEmpty() || item.field.isEmpty()) {
            style = WeekViewEntity.Style.Builder().setBackgroundColor(Color.GRAY).build()
        }

        /*
        var style: WeekViewEntity.Style = if (item.booked)
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.GREEN).build()
        else if (item.fields.size == 0 || item.bookedByOther || item.userHasSport)
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.RED).build()
        else
            WeekViewEntity.Style.Builder().setBackgroundColor(Color.GRAY).build()

        if (item.startTime < Calendar.getInstance())
            style = WeekViewEntity.Style.Builder().setBackgroundColor(Color.RED).build()

         */
        val startLocalDateTime = LocalDateTime.parse(item.start_at)
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, startLocalDateTime.year)
            set(Calendar.MONTH, startLocalDateTime.month.value - 1)
            set(Calendar.DATE, startLocalDateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, startLocalDateTime.hour)
            set(Calendar.MINUTE, startLocalDateTime.minute)
            set(Calendar.SECOND, startLocalDateTime.second)
        }

        val endLocalDateTime = LocalDateTime.parse(item.ends_at)
        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, endLocalDateTime.year)
            set(Calendar.MONTH, endLocalDateTime.month.value - 1)
            set(Calendar.DATE, endLocalDateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, endLocalDateTime.hour)
            set(Calendar.MINUTE, endLocalDateTime.minute)
            set(Calendar.SECOND, endLocalDateTime.second)
        }
        return WeekViewEntity.Event.Builder(item)
            .setId(startCalendar.timeInMillis)//TODO renderlo dinamico
            .setTitle("")
            .setStartTime(startCalendar)
            .setEndTime(endCalendar)
            .setStyle(style)
            .build()
    }

    override fun onEventClick(data: BookableTimeslot) {
        if (data.field.filter { it.value.booked_by == viewModel.userEmail || it.value.joined_players.contains(viewModel.userEmail) }.isNotEmpty()) {
            val builder = AlertDialog.Builder(this.context)
            builder.setMessage("You already booked this timeslot!")
            builder.setNegativeButton("OK") { _, _ ->
            }
            builder.show()
            return
        }
        if (data.field.filter { it.value.booked_by == null }.isEmpty()) {
            val builder = AlertDialog.Builder(this.context)
            builder.setMessage("No fields available!")
            builder.setNegativeButton("OK") { _, _ ->
            }
            builder.show()
            return
        }
        val bundle = Bundle()
        bundle.putString(
            "reservation",
            Json.encodeToString(data)
        )
        bundle.putString(
            "selectedField",
            Json.encodeToString(viewModel.gSelectedField.value to data.field[viewModel.gSelectedField.value])
        )
        navController.navigate(
            R.id.action_new_reservation_fragment_to_confirmReservationFragment2,
            bundle
        )
        /*
        val startTimeFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val endTimeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm")
        val strDate1: String = startTimeFormat.format(data.startTime.time)
        val strDate2: String = endTimeFormat.format(data.endTime.time)
        lateinit var confirmreservationdata: ConfirmReservationData

        if (data.startTime > Calendar.getInstance()) {
            if (!data.userHasSport && !data.booked) { //!data.userHasSport &&
                if (data.fields.size == 0 || data.bookedByOther) {
                    val builder = AlertDialog.Builder(this.context)

                    builder.setTitle("Error")
                    builder.setMessage("Timeslot not available")

                    builder.setNegativeButton("OK") { _, _ ->
                    }

                    builder.show()
                } else {
                    if (!data.calTitle.equals(data.sportName)) {
                        confirmreservationdata = ConfirmReservationData(
                            sportField = data.fields[0],
                            sportName = data.sportName,
                            startTime = strDate1,
                            endTime = strDate2,
                            timeSlotId = data.id.toInt()
                        )

                        val bundle = Bundle()
                        bundle.putString(
                            "reservation",
                            Json.encodeToString(confirmreservationdata)
                        )
                        navController.navigate(
                            R.id.action_new_reservation_fragment_to_confirmReservationFragment2,
                            bundle
                        )
                    } else {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("$strDate1 - $strDate2")

                        val bookableReservations = arrayOfNulls<String>(data.fields.size)
                        var i = 0
                        data.fields.forEach {
                            bookableReservations[i] = it.name
                            i++;
                        }

                        val checkedReservations = BooleanArray(data.fields.size) { false }

                        builder.setMultiChoiceItems(bookableReservations, checkedReservations,
                            OnMultiChoiceClickListener { dialog, which, isChecked ->
                                // The user checked or unchecked a box
                                checkedReservations[which] = isChecked
                                i = 0
                                for (i in 0..data.fields.size - 1) {
                                    if (i != which) {
                                        checkedReservations[i] = false
                                        (dialog as AlertDialog).listView.setItemChecked(
                                            i,
                                            false
                                        )
                                    }
                                }
                                confirmreservationdata = ConfirmReservationData(
                                    sportField = data.fields[which],
                                    sportName = data.sportName,
                                    startTime = strDate1,
                                    endTime = strDate2,
                                    timeSlotId = data.id.toInt()
                                )
                            })

                        builder.setPositiveButton("OK") { _, _ ->
                            if (checkedReservations.contains(true)) {
                                //val intent = Intent(context, ConfirmReservation::class.java)
                                //Pass any necessary data to ConfirmReservation activity
                                //intent.putExtra(
                                //    "reservation",
                                //    Json.encodeToString(confirmreservationdata)
                                //)
                                //context.startActivity(intent)

                                val bundle = Bundle()
                                bundle.putString(
                                    "reservation",
                                    Json.encodeToString(confirmreservationdata)
                                )
                                navController.navigate(
                                    R.id.action_new_reservation_fragment_to_confirmReservationFragment2,
                                    bundle
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Select a sport field",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                        builder.setNegativeButton("BACK", null)

                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            } else {
                //caso cal generale
                if (data.sportName.equals(data.calTitle)) {
                    if (data.booked) {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has already booked a field for this sport")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    }
                    if (data.userHasSport) {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has another reservation for this timeslot")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    }
                    //caso filtro sport
                } else {
                    if (data.booked) {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has already booked this field")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    } else {
                        val builder = AlertDialog.Builder(this.context)

                        builder.setTitle("Error")
                        builder.setMessage("User has already booked another field")

                        builder.setNegativeButton("OK") { _, _ ->
                        }

                        builder.show()
                    }
                }
            }
        } else {
            val builder = AlertDialog.Builder(this.context)

            builder.setTitle("Error")
            builder.setMessage("User cannot choose a past timeslot")

            builder.setNegativeButton("OK") { _, _ ->
            }

            builder.show()
        }
        */
    }
}