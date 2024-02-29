package it.polito.mad.g22.showprofileactivity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.FirebaseSportEvaluation
import it.polito.mad.g22.showprofileactivity.data.Sport
import it.polito.mad.g22.showprofileactivity.data.sportEvaluation

class SportEvaluationShowAdapter(
    val evaluations: List<FirebaseSportEvaluation>,
    val context: Context
) : RecyclerView.Adapter<SportEvaluationShowAdapter.SportEvaluationShowHolder>() {

    class SportEvaluationShowHolder(v : View) : RecyclerView.ViewHolder(v){
        val sportName = v.findViewById<TextView>(R.id.evaluation_sport_name)
        val starsNumber = v.findViewById<TextView>(R.id.evaluation_stars_number)
        val image = v.findViewById<ImageView>(R.id.evaluation_image)
        val clickableArea = v.findViewById<LinearLayout>(R.id.evaluation_clickable_linear_layout)
        fun bind(eval: FirebaseSportEvaluation){
            clickableArea.isClickable = false
            image.setImageURI("android.resource://it.polito.mad.g22.showprofileactivity/drawable/${eval.sport_drawable}".toUri())
            sportName.text = eval.sport_name
            starsNumber.text = "${eval.stars} ★"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportEvaluationShowHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_evaluation_layout,parent,false)
        return SportEvaluationShowHolder(v)
    }

    override fun onBindViewHolder(holder: SportEvaluationShowHolder, position: Int) {
        val evaluation = evaluations[position]
        holder.bind(evaluation)
    }

    override fun getItemCount() =  evaluations.size

}