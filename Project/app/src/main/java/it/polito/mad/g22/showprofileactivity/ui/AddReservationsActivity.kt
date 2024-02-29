package it.polito.mad.g22.showprofileactivity.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.alamkanak.weekview.WeekView
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.adapter.FieldCalendarAdapter
import it.polito.mad.g22.showprofileactivity.adapter.ShowFieldsAdapter
import it.polito.mad.g22.showprofileactivity.adapter.ShowSportsAdapter
import it.polito.mad.g22.showprofileactivity.adapter.SportCalendarAdapter
import it.polito.mad.g22.showprofileactivity.data.Sport
import it.polito.mad.g22.showprofileactivity.model.AddReservationViewModel
import java.time.LocalDateTime

class AddReservationsActivity() : Fragment(R.layout.activity_add_reservations) {
    val viewModel by viewModels<AddReservationViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showSportsRecyclerView =
            requireView().findViewById<RecyclerView>(R.id.show_sports_recycler_view)
        val weekView2 = requireView().findViewById<WeekView>(R.id.weekView2)
        val titleFields = requireView().findViewById<TextView>(R.id.titleFields)
        val noSelectedSport = requireView().findViewById<LinearLayout>(R.id.noSelectedSport)
        val showFieldsRecyclerView =
            requireView().findViewById<RecyclerView>(R.id.show_fields_recycler_view)
        val loadingLayout =
            requireView().findViewById<LinearLayout>(R.id.add_reservation_loading_layout)
        val pageLayout = requireView().findViewById<LinearLayout>(R.id.add_reservation_page_layout)


        viewModel.loadedData.observe(viewLifecycleOwner) {
            if (it >= viewModel.expectedData) {
                val sportList = viewModel.sportMap.map {
                    Sport(
                        0,
                        it.key,
                        it.value.drawable
                    )
                }
                showSportsRecyclerView.adapter = ShowSportsAdapter(sportList, viewModel)
                loadingLayout.visibility = View.GONE
                pageLayout.visibility = View.VISIBLE

            }

            val sportCalendarAdapter = SportCalendarAdapter(findNavController(), viewModel)
            val fieldCalendarAdapter = FieldCalendarAdapter(findNavController(), viewModel)
            viewModel.sportAndFieldMediator.observe(viewLifecycleOwner) {
                if (it == "Sport") {
                    showFieldsRecyclerView.adapter = ShowFieldsAdapter(
                        viewModel.fieldMap.filter { it.value.sport.name == viewModel.gSelectedSport.value!! },
                        viewModel
                    )
                    weekView2.adapter = sportCalendarAdapter
                    sportCalendarAdapter.submitList(
                        viewModel.gSportFilteredTimeslot.value!!.values.filter {
                            it.start_at > LocalDateTime.now().toString()
                        }.toList()
                    )

                    weekView2.visibility = View.VISIBLE
                    titleFields.visibility = View.VISIBLE
                    noSelectedSport.visibility = View.GONE

                } else if (it == "Field") {
                    weekView2.adapter = fieldCalendarAdapter
                    fieldCalendarAdapter.submitList(
                        viewModel.gFieldFilteredTimeslot.value!!.values.filter {
                            it.start_at > LocalDateTime.now().toString()
                        }.toList()
                    )
                }
            }

            viewModel.timeSlotMap.observe(viewLifecycleOwner) {
                if (viewModel.sportAndFieldMediator.value == "Sport") {
                    sportCalendarAdapter.submitList(viewModel.gSportFilteredTimeslot.value!!.values.toList())
                } else if (viewModel.sportAndFieldMediator.value == "Field") {
                    fieldCalendarAdapter.submitList(viewModel.gFieldFilteredTimeslot.value!!.values.toList())
                }
            }
        }
    }
}