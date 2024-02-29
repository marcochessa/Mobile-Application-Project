package it.polito.mad.g22.showprofileactivity.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.TimeSlotField
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JoinReservationAdapter(
    var joinReservations: List<Pair<String, Map.Entry<String, TimeSlotField>>>,
    val navController: NavController
) :
    RecyclerView.Adapter<JoinReservationAdapter.JoinViewHolder>() {

    class JoinViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val sportName = v.findViewById<TextView>(R.id.sportName)
        val drawableSport = v.findViewById<ImageView>(R.id.sportImage)
        val fieldName = v.findViewById<TextView>(R.id.fieldName)
        val addressField = v.findViewById<TextView>(R.id.addressField)

        val reservationDate = v.findViewById<TextView>(R.id.reservationDate)
        val reservationTimeslot = v.findViewById<TextView>(R.id.reservationTimeslot)
        val card = v.findViewById<CardView>(R.id.reservation_item_card)
        fun bind(item: Pair<String, Map.Entry<String, TimeSlotField>>, position: Int, onTap: (Int) -> Unit) {
            sportName.text = item.second.value.sport.name
            drawableSport.setImageURI("android.resource://it.polito.mad.g22.showprofileactivity/drawable/black_${item.second.value.sport.drawable}".toUri())
            fieldName.text = item.second.key
            addressField.text = item.second.value.address
            reservationDate.text =
                LocalDateTime.parse(item.first.split(' ')[0])
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            reservationTimeslot.text = "${
                LocalDateTime.parse(item.first.split(' ')[0])
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            } - ${
                LocalDateTime.parse(item.first.split(' ')[1])
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            }"

            card.setOnClickListener { onTap(position) }
        }

        fun unbind() {
            super.itemView.setOnClickListener(null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JoinViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return JoinViewHolder(v)
    }

    override fun getItemCount(): Int {
        return joinReservations.size
    }

    override fun onBindViewHolder(holder: JoinViewHolder, position: Int) {
        val item = joinReservations[position]
        holder.bind(item, position) {
            val builder = AlertDialog.Builder(holder.itemView.context)

            builder.setMessage("Do you want to join this reservation?")
            builder.setPositiveButton("YES") { _, _ ->
                val bundle = Bundle()
                bundle.putString("joinreservation", Json.encodeToString(item))
                navController.navigate(
                    R.id.action_joinReservationFragment_to_confirmJoinReservation,
                    bundle
                )
            }

            builder.setNegativeButton("NO") { _, _ ->
            }

            builder.show()

        }
    }

    override fun onViewRecycled(holder: JoinViewHolder) {
        holder.unbind()
    }
}