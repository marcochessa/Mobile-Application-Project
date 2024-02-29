package it.polito.mad.g22.showprofileactivity.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.FirebaseSportField
import it.polito.mad.g22.showprofileactivity.model.AddReservationViewModel

class ShowFieldsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val showFieldName = v.findViewById<TextView>(R.id.showFieldName)
    val show_single_field = v.findViewById<LinearLayout>(R.id.show_single_field)

    fun bind(item: Pair<String, FirebaseSportField?>, position: Int, onTap: (Int) -> Unit) {
        showFieldName.text = item.first

        super.itemView.setOnClickListener { onTap(position) }
    }

    fun unbind() {
        super.itemView.setOnClickListener(null)
    }
}

class ShowFieldsAdapter(
    var sportFields: Map<String, FirebaseSportField>,
    val viewModel: AddReservationViewModel
) :
    RecyclerView.Adapter<ShowFieldsViewHolder>() {
    var selectedPosition = -1
    var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowFieldsViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_show_fields, parent, false)
        return ShowFieldsViewHolder(v)
    }

    override fun getItemCount(): Int {
        return sportFields.size
    }

    override fun onBindViewHolder(holder: ShowFieldsViewHolder, position: Int) {
        val item =
            sportFields.keys.toTypedArray()[position] to sportFields.get(sportFields.keys.toTypedArray()[position])
        holder.bind(item, position) {
            lastSelectedPosition = selectedPosition
            selectedPosition = holder.bindingAdapterPosition

            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)


            //vm3.sentField(item.name)
            if (selectedPosition == lastSelectedPosition) {
                viewModel.gSelectedSport.value = viewModel.gSelectedSport.value
            }
            else {
                viewModel.gSelectedField.value = item.first
            }
        }
        if (selectedPosition == lastSelectedPosition) {
            holder.show_single_field.setBackgroundColor(Color.WHITE);
            selectedPosition = -1
            lastSelectedPosition = -1
            //vm2.choosenSport.value?.let { vm2.sentSport(it, false) }
        } else {
            holder.show_single_field.setBackgroundColor(Color.LTGRAY);

            if (selectedPosition == holder.bindingAdapterPosition) {
                holder.show_single_field.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.show_single_field.setBackgroundColor(Color.WHITE);
            }


        }
    }



override fun onViewRecycled(holder: ShowFieldsViewHolder) {
    holder.unbind()
}
}