package it.polito.mad.g22.showprofileactivity.adapter

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.FirebaseSport
import it.polito.mad.g22.showprofileactivity.data.FirebaseSportEvaluation
import it.polito.mad.g22.showprofileactivity.data.Sport
import it.polito.mad.g22.showprofileactivity.model.ProfileEditPageViewModel

class SportEvaluationAdapter(
    val evaluations: List<FirebaseSportEvaluation>,
    val sports: Map<String, FirebaseSport>,
    val context: Context,
    val viewModel: ProfileEditPageViewModel
) : RecyclerView.Adapter<SportEvaluationAdapter.SportEvaluationBasicHolder>() {


    abstract class SportEvaluationBasicHolder(v: View) : RecyclerView.ViewHolder(v) {
        abstract fun bind(
            eval: FirebaseSportEvaluation? = null, sport: Sport? = null,
            callback: ((Sport, FirebaseSportEvaluation) -> Unit)? = null,
            addButton: Boolean = false, addCallback: (() -> Unit)? = null
        )
    }

    class SportEvaluationViewHolder(v: View) : SportEvaluationBasicHolder(v) {
        val sportName = v.findViewById<TextView>(R.id.evaluation_sport_name)
        val starsNumber = v.findViewById<TextView>(R.id.evaluation_stars_number)
        val image = v.findViewById<ImageView>(R.id.evaluation_image)
        val clickableArea = v.findViewById<LinearLayout>(R.id.evaluation_clickable_linear_layout)
        override fun bind(
            eval: FirebaseSportEvaluation?,
            sport: Sport?,
            callback: ((Sport, FirebaseSportEvaluation) -> Unit)?,
            addButton: Boolean,
            addCallback: (() -> Unit)?
        ) {
            image.setImageURI("android.resource://it.polito.mad.g22.showprofileactivity/drawable/${eval!!.sport_drawable}".toUri())
            sportName.text = eval!!.sport_name
            starsNumber.text = "${eval!!.stars.toString()} ★"
            clickableArea.setOnClickListener { callback!!(sport!!, eval) }
        }
    }

    class AddItemViewHolder(v: View) : SportEvaluationBasicHolder(v) {
        val clickableArea = v.findViewById<ConstraintLayout>(R.id.add_evaluation_click_area)
        override fun bind(
            eval: FirebaseSportEvaluation?,
            sport: Sport?,
            callback: ((Sport, FirebaseSportEvaluation) -> Unit)?,
            addButton: Boolean,
            addCallback: (() -> Unit)?
        ) {
            clickableArea.setOnClickListener { addCallback!!() }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (sports.size != evaluations.size && position == itemCount - 1) {
            1
        } else {
            0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportEvaluationBasicHolder {
        if (viewType == 0) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.single_evaluation_layout, parent, false)
            return SportEvaluationViewHolder(v)
        } else {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.evaluation_add_layout, parent, false)
            return AddItemViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: SportEvaluationBasicHolder, position: Int) {
        if (sports.size != evaluations.size && position == itemCount - 1) {
            holder.bind(addButton = true, addCallback = this::showAddEvaluationDialog)
        } else {
            val evaluation = evaluations[position]
            val sportList = sports.map {
                Sport(
                    0,
                    it.key,
                    it.value.drawable
                )
            }
            holder.bind(evaluation, sportList.first(), this::showEvaluationDialog)
        }

    }

    override fun getItemCount() =
        if (sports.size != evaluations.size) evaluations.size + 1 else evaluations.size

    fun showEvaluationDialog(sport: Sport, evaluation: FirebaseSportEvaluation) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.custom_dialog)
        val confirmButton = dialog.findViewById<Button>(R.id.custom_dialog_button)
        val cancelButton = dialog.findViewById<Button>(R.id.evaluation_dialog_cancel_button)
        val deleteButton = dialog.findViewById<Button>(R.id.evaluation_dialog_delete_button)
        val sportName = dialog.findViewById<TextView>(R.id.evaluation_dialog_sport_name)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.evaluation_dialog_ratingbar)
        sportName.text = evaluation.sport_name
        ratingBar.rating = evaluation.stars.toFloat()
        confirmButton.setOnClickListener {
            /*val modifiedUser = viewModel.userLiveData.value!!
            modifiedUser.evaluation.forEach {
                if(it.sport_name == evaluation.sport_name){
                    it.stars = ratingBar.rating.toInt()
                }
            }
            viewModel.userLiveData.value = modifiedUser

             */
            viewModel.evaluationLiveData.value!!.forEach {
                if(it.sport_name == evaluation.sport_name){
                    it.stars = ratingBar.rating.toInt()
                }
            }
            dialog.dismiss()//+
            //FirebaseSportEvaluation(sport.name, sport.drawable, re)
            //TODO
            /* if(it.sportId == evaluation.sport) {
                    it.evaluation = ratingBar.rating.toInt()
                    it
                }
                else
                {
                    it
                }
                */

            dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        deleteButton.setOnClickListener {
            //val modifiedUser = viewModel.userLiveData.value!!
            viewModel.evaluationLiveData.value = viewModel.evaluationLiveData.value!!.filter { it.sport_name != evaluation.sport_name }
            //modifiedUser.evaluation = modifiedUser.evaluation.filter {it.sport_name != evaluation.sport_name}
            //viewModel.userLiveData.value = modifiedUser  //TODO it.sport != sport.id }
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showAddEvaluationDialog() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.add_evaluation_dialog)
        val spinner = dialog.findViewById<Spinner>(R.id.add_evaluation_sport_spinner)
        val spinnerAdapter = ArrayAdapter(
            context,
            R.layout.custom_spinner_evaluation,
            viewModel.sports.keys.filter {
                !viewModel.userLiveData.value!!.evaluation.map { it.sport_name }.contains(it)
            })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        val confirmButton = dialog.findViewById<Button>(R.id.add_evaluation_confirm_button)
        val cancelButton = dialog.findViewById<Button>(R.id.add_evaluation_cancel_button)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.add_evaluation_dialog_rating_bar)
        confirmButton.setOnClickListener {
            /*val modifiedUser = viewModel.userLiveData.value!!
            modifiedUser.evaluation = modifiedUser.evaluation + FirebaseSportEvaluation(
                spinner.selectedItem.toString(),
                sports[spinner.selectedItem.toString()]!!.drawable,
                ratingBar.rating.toInt()
            )
            viewModel.userLiveData.postValue(modifiedUser)

             */
            viewModel.evaluationLiveData.value = viewModel.evaluationLiveData.value!! + FirebaseSportEvaluation(
                spinner.selectedItem.toString(),
                sports[spinner.selectedItem.toString()]!!.drawable,
                ratingBar.rating.toInt()
            )
            dialog.dismiss()
            /*val evaluationToBeAdded = FirebaseSportEvaluation(
                                        FirebaseSport(0, "db"),
                                        ratingBar.rating.toInt())
            viewModel.userLiveData.value!!.evaluation = viewModel.userLiveData.value!!.evaluation + evaluationToBeAdded
            dialog.dismiss()

             */
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

}