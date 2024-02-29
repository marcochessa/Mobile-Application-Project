package it.polito.mad.g22.showprofileactivity.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.model.JoinReservationViewModel
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.adapter.JoinReservationAdapter
import it.polito.mad.g22.showprofileactivity.data.TimeSlotField
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JoinReservationFragment : Fragment(R.layout.join_reservation_fragment) {
    val viewmodel by viewModels<JoinReservationViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewJoin = requireView().findViewById<RecyclerView>(R.id.recyclerViewJoin)
        val noJoin = requireView().findViewById<RelativeLayout>(R.id.noJoin)
        val spinnerLoading = requireView().findViewById<LinearLayout>(R.id.join_reservation_loading_layout)
        val mainLayout = requireView().findViewById<FrameLayout>(R.id.mainLayout)

        viewmodel.joinableTimeslot.observe(viewLifecycleOwner) {
            Log.d("Just debugging", "Debugging")
            val adapterMap = it.flatMap { timeslot ->
                timeslot.field.map {
                    "${timeslot.start_at} ${timeslot.ends_at}" to it
                }
            }
            recyclerViewJoin.adapter = JoinReservationAdapter(adapterMap,findNavController())
            spinnerLoading.visibility = View.GONE
            mainLayout.visibility = View.VISIBLE
            if(adapterMap.isNotEmpty())
                noJoin.visibility = View.GONE
            else
                noJoin.visibility = View.VISIBLE
            recyclerViewJoin.layoutManager = LinearLayoutManager(context)
        }
    }
}
