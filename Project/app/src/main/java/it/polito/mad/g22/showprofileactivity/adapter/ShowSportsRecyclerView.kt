package it.polito.mad.g22.showprofileactivity.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.Sport
import it.polito.mad.g22.showprofileactivity.model.AddReservationViewModel

class ShowSportsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    val showSportIcon = v.findViewById<ImageView>(R.id.showSportIcon)
    val showSportName = v.findViewById<TextView>(R.id.showSportName)
    val show_single_sport = v.findViewById<LinearLayout>(R.id.show_single_sport)

    fun bind(item: Sport, position: Int, onTap: (Int) -> Unit) {
        showSportIcon.setImageURI("android.resource://it.polito.mad.g22.showprofileactivity/drawable/black_${item.drawable!!}".toUri())
        showSportName.text = item.name

        super.itemView.setOnClickListener { onTap(position) }
    }

    fun unbind() {
        super.itemView.setOnClickListener(null)
    }
}

class ShowSportsAdapter(var sports: List<Sport>, val vm2: AddReservationViewModel) :
    RecyclerView.Adapter<ShowSportsViewHolder>() {
    var selectedPosition = -1
    var lastSelectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowSportsViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_layout_show_sports, parent, false)
        return ShowSportsViewHolder(v)
    }

    override fun getItemCount(): Int {
        return sports.size
    }

    override fun onBindViewHolder(holder: ShowSportsViewHolder, position: Int) {
        val item = sports[position]
        holder.bind(item, position) {
            lastSelectedPosition = selectedPosition;
            selectedPosition = holder.bindingAdapterPosition;
            notifyItemChanged(lastSelectedPosition);
            notifyItemChanged(selectedPosition);

            vm2.gSelectedSport.value = item.name
            //vm2.sentSport(item.name, true)
        }
        if (selectedPosition == holder.bindingAdapterPosition) {
            holder.show_single_sport.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.show_single_sport.setBackgroundColor(Color.WHITE);
        }
    }

    override fun onViewRecycled(holder: ShowSportsViewHolder) {
        holder.unbind()
    }
}