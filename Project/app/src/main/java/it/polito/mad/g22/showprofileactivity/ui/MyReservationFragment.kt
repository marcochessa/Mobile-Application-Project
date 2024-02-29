package it.polito.mad.g22.showprofileactivity.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.model.ReservationViewModel
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.FirebaseUserBooking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : Fragment(R.layout.my_reservation_fragment) {
    val vm by viewModels<ReservationViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerView)
        val noItems = requireView().findViewById<RelativeLayout>(R.id.noItems)
        val spinnerLoading = requireView().findViewById<LinearLayout>(R.id.my_reservation_loading_layout)
        val mainLayout = requireView().findViewById<FrameLayout>(R.id.my_reservation_page_layout)

        vm.userLiveData.observe(viewLifecycleOwner) {
            spinnerLoading.visibility = View.GONE
            mainLayout.visibility = View.VISIBLE
            if (it!!.booking.isNotEmpty()) {
                recyclerView.adapter = MyAdapter(
                    it.booking.filter { it.start_at > LocalDateTime.now().toString() },
                    findNavController()
                )
                recyclerView.layoutManager = LinearLayoutManager(context)
                noItems.visibility = View.GONE
            } else
                noItems.visibility = View.VISIBLE
        }
    }

}


class MyAdapter(var myReservations: List<FirebaseUserBooking>, val navController: NavController) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val sportName = v.findViewById<TextView>(R.id.sportName)
        val drawableSport = v.findViewById<ImageView>(R.id.sportImage)
        val fieldName = v.findViewById<TextView>(R.id.fieldName)
        val addressField = v.findViewById<TextView>(R.id.addressField)

        val reservationDate = v.findViewById<TextView>(R.id.reservationDate)
        val reservationTimeslot = v.findViewById<TextView>(R.id.reservationTimeslot)
        val card = v.findViewById<CardView>(R.id.reservation_item_card)
        val joinedLabelLayout = v.findViewById<LinearLayout>(R.id.joined_label_layout)
        fun bind(item: FirebaseUserBooking, position: Int, onTap: (Int) -> Unit) {
            sportName.text = item.field.sport.name
            drawableSport.setImageURI("android.resource://it.polito.mad.g22.showprofileactivity/drawable/black_${item.field.sport.drawable}".toUri())
            fieldName.text = item.field.name
            addressField.text = item.field.address
            LocalDateTime.parse(item.start_at)
            reservationDate.text =
                LocalDateTime.parse(item.start_at).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            reservationTimeslot.text = "${
                LocalDateTime.parse(item.start_at).format(DateTimeFormatter.ofPattern("HH:mm"))
            } - ${
                LocalDateTime.parse(item.ends_at).format(DateTimeFormatter.ofPattern("HH:mm"))
            }"
            if(!item.booking_owner){
                joinedLabelLayout.visibility = View.VISIBLE
            }

            card.setOnClickListener { onTap(position) }
        }

        fun unbind() {
            super.itemView.setOnClickListener(null)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return myReservations.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = myReservations[position]
        holder.bind(item, position) {
            val bookingOwner = item.booking_owner
            val builder = AlertDialog.Builder(holder.itemView.context)

            builder.setMessage("Do you want to edit this reservation?")
            builder.setPositiveButton("EDIT") { _, _ ->
                val bundle = Bundle()
                bundle.putString("myreservation", Json.encodeToString(item))
                if (bookingOwner) {
                    navController.navigate(
                        R.id.action_my_reservation_fragment_to_editReservationFragment,
                        bundle
                    )
                } else {
                    navController.navigate(
                        R.id.action_my_reservation_fragment_to_editJoinedReservationFragment,
                        bundle
                    )
                }
            }

            builder.setNegativeButton("BACK") { _, _ ->
            }

            builder.show()

        }
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        holder.unbind()
    }

}