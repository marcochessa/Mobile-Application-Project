package it.polito.mad.g22.showprofileactivity.ui

import android.app.Application
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.adapter.SportEvaluationShowAdapter
import it.polito.mad.g22.showprofileactivity.data.*
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class ShowProfileActivityViewModel(application: Application) : AndroidViewModel(application){
    val firebaseDb = Firebase.firestore
    var userLiveData: MutableLiveData<FirebaseUser> = MutableLiveData<FirebaseUser>()
    val userEmail = SavedPreference.getEmail(application)!!
    init {
        firebaseDb.collection("user")
            .document(userEmail)
            .addSnapshotListener { snapshot, e ->
                if(e!=null){
                    throw Exception("Problem with user query")
                }
                userLiveData.value = snapshot!!.toObject(FirebaseUser::class.java)
            }
    }
}

class ShowProfileActivity : Fragment(R.layout.show_profile_activity) {

    val viewModel: ShowProfileActivityViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.show_profile_recycler_view)
        val imageView = requireView().findViewById<ImageView>(R.id.avatarImageView)

        viewModel.userLiveData.observe(viewLifecycleOwner){
            updateView(it)
            //show_profile_recycler_view
            if(viewModel.userLiveData.value!!.evaluation.isEmpty()){
                requireView().findViewById<TextView>(R.id.no_evaluation_yet_text).visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            else{
                val adapter = SportEvaluationShowAdapter(viewModel.userLiveData.value!!.evaluation, requireContext())
                recyclerView.adapter = adapter
            }

            if(viewModel.userLiveData.value!!.image.contains("google")){
                imageView.setImageURI(null)
                imageView.setImageURI(viewModel.userLiveData.value!!.image.toUri())
                Glide.with(this)
                    .load(viewModel.userLiveData.value!!.image)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView)
            }
            else{
                imageView.setImageURI(null)
                imageView.setImageURI(viewModel.userLiveData.value!!.image.toUri())
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu,inflater)
        val inflater: MenuInflater = inflater
        inflater.inflate(R.menu.editmenu_showprofileactivity, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.editPage -> {
                findNavController().navigate(R.id.action_showProfileActivity_to_editPageActivity2)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun updateView(u: FirebaseUser){
        val d = LocalDate.parse(u.birthDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        requireView().findViewById<TextView>(R.id.fullnameTextView)!!.text = u.fullname
        requireView().findViewById<TextView>(R.id.nickNameTextView)!!.text = u.nickname
        requireView().findViewById<TextView>(R.id.ageTextView)!!.text = Period.between(
                d,
                LocalDate.now()
            ).years.toString()
        requireView().findViewById<TextView>(R.id.biotextView)!!.text = u.bio
        requireView().findViewById<LinearLayout>(R.id.flagsContainer)?.removeAllViews()

        for (lan in viewModel.userLiveData.value!!.languages){
            val imageDimdip = 28f
            val imageMardip = 5f
            val r: Resources = resources
            val imageDimPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                imageDimdip,
                r.displayMetrics
            )
            val imageMarPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                imageMardip,
                r.displayMetrics
            )
            val iv = ImageView(this.context)
            val params = LinearLayout.LayoutParams(imageDimPx.toInt(),imageDimPx.toInt())
            params.leftMargin = imageMarPx.toInt()
            params.rightMargin = imageMarPx.toInt()
            iv.layoutParams = params
            iv.setImageURI(Uri.parse("android.resource://it.polito.mad.g22.showprofileactivity/drawable/"+lan.lowercase()))
            requireView().findViewById<LinearLayout>(R.id.flagsContainer)?.addView(iv)
        }
    }
}