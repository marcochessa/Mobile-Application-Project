package it.polito.mad.g22.showprofileactivity.adapter

import android.content.Context
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.ui.ReviewViewModel
import it.polito.mad.g22.showprofileactivity.data.FirebaseUserBooking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ReviewAdapter(
    val reviews: List<FirebaseUserBooking>,
    val context: Context,
    val viewModel: ReviewViewModel
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val sportFieldName = v.findViewById<TextView>(R.id.textViewSportField)
        val date = v.findViewById<TextView>(R.id.date)
        val owner = v.findViewById<TextView>(R.id.owner)
        val rating = v.findViewById<TextView>(R.id.textViewSportFieldRate)
        val reviewRow = v.findViewById<LinearLayout>(R.id.singleReviewRow)
        fun bind(
            review: FirebaseUserBooking,
            /*sportField: SportField,*/
            callback: ((FirebaseUserBooking) -> Unit)? = null
        ) {
            sportFieldName.text = review.field.name
            date.text = "${LocalDateTime.parse(review.start_at).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))} - " +
                        "${LocalDateTime.parse(review.ends_at).format(DateTimeFormatter.ofPattern("HH:mm"))}"
            owner.text = review.booking_owner.toString()
            if(review.review != null) {
                rating.text = "${review.review} ★"
            }
            reviewRow.setOnClickListener { callback!!(review) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_review_layout, parent, false)
        return ReviewViewHolder(v)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.bind(
            review,
            this::showReviewDialog
        )
    }

    override fun getItemCount() =  reviews.size

    fun showReviewDialog(review: FirebaseUserBooking) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.review_dialog)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButtonReview)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButtonReview)
        val sportFieldName = dialog.findViewById<TextView>(R.id.sportFieldDialogTextView)
        val date = dialog.findViewById<TextView>(R.id.date)
        val description = dialog.findViewById<EditText>(R.id.descriptionReviewEditText)
        val deleteButton = dialog.findViewById<ImageView>(R.id.deleteButtonReview)
        val ratingBar = dialog.findViewById<RatingBar>(R.id.ratingBarReview)
        if(review.review!=null){
            deleteButton.visibility = View.VISIBLE
        }

        sportFieldName.text = review.field.name
        date.text = "${LocalDateTime.parse(review.start_at).format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm"))}"
        /*if(review.description!=null)
            description.setText(review.description)

         */
        ratingBar.rating = review.review?.toFloat() ?: 0f
        confirmButton.setOnClickListener {
            confirmButton.text = "Loading..."
            confirmButton.isClickable = false
            deleteButton.isClickable = false
            cancelButton.isClickable = false
            viewModel.updateReview(review, ratingBar.rating, dialog)
        }
        deleteButton.setOnClickListener {
            viewModel.deleteReview(review, dialog)
            dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }
}