package it.polito.mad.g22.showprofileactivity.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.polito.mad.g22.showprofileactivity.FragmentNavigationContainerActivity
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.data.FirebaseUserBooking

class HomeFragment : Fragment(R.layout.home_page) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show();
    }
    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.show();

        requireView().findViewById<CardView>(R.id.joinMatch).setOnClickListener {
            findNavController().navigate(R.id.joinReservationFragment)
        }

        requireView().findViewById<RelativeLayout>(R.id.northwest).setOnClickListener {
            it.animate().apply {
                duration = 500
                scaleX(0.8f)
                scaleY(0.8f)
            }.withEndAction {
                findNavController().navigate(R.id.my_reservation_fragment)
            }
        }

        requireView().findViewById<RelativeLayout>(R.id.northeast).setOnClickListener {
            it.animate().apply {
                duration = 500
                scaleX(0.8f)
                scaleY(0.8f)
            }.withEndAction {
                findNavController().navigate(R.id.new_reservation_fragment)
            }
        }

        requireView().findViewById<RelativeLayout>(R.id.southwest).setOnClickListener {
            it.animate().apply {
                duration = 500
                scaleX(0.8f)
                scaleY(0.8f)
            }.withEndAction {
                findNavController().navigate(R.id.review_fragment)
            }
        }

        requireView().findViewById<RelativeLayout>(R.id.southeast).setOnClickListener {
            it.animate().apply {
                duration = 500
                scaleX(0.8f)
                scaleY(0.8f)
            }.withEndAction {
                findNavController().navigate(R.id.showProfileActivity)
            }
        }

        val callback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                showAreYouSureDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, // LifecycleOwner
            callback
        )
    }

    fun showAreYouSureDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.exit_dialog)
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val exitButton = dialog.findViewById<Button>(R.id.exitButton)

        exitButton.setOnClickListener {
            dialog.dismiss()
            requireActivity().finish()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}