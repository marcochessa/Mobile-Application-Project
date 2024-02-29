package it.polito.mad.g22.showprofileactivity.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.telephony.PhoneNumberUtils
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.adapter.SportEvaluationAdapter
import it.polito.mad.g22.showprofileactivity.data.*
import it.polito.mad.g22.showprofileactivity.model.ProfileEditPageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileDescriptor
import java.io.IOException
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


val storageRef = Firebase.storage.reference




class EditProfileFragment : Fragment(R.layout.activity_edit_page) {

    val viewModel by viewModels<ProfileEditPageViewModel>()

    private lateinit var imageView: ImageView


    private val galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.imageUri = it.data?.data!!

                val c = requireActivity()
                val f = c.contentResolver.openInputStream(viewModel.imageUri!!)
                val pathSplitted = viewModel.imageUri!!.path!!.split("/")
                val filename = pathSplitted[pathSplitted.size-1]
                c.openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write(f!!.readBytes()!!)
                }
                imageView.setImageURI(Uri.parse(c.filesDir.toString()+"/"+filename)!!)
            }
        }

    private val cameraActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val c = requireActivity()
                val f = c.contentResolver.openInputStream(viewModel.imageUri!!)
                val pathSplitted = viewModel.imageUri!!.path!!.split("/")
                val filename = pathSplitted[pathSplitted.size-1]
                c.openFileOutput(filename, Context.MODE_PRIVATE).use {
                    it.write(f?.readBytes()!!)
                }
                imageView.setImageURI(Uri.parse(c.filesDir.toString()+"/"+filename)!!)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments?.getString("newUser")!=null) {
            (requireActivity() as AppCompatActivity).supportActionBar?.show()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        if(arguments?.getString("newUser")!=null) {
            (requireActivity() as AppCompatActivity).supportActionBar?.show()
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Please insert your data to sign up in GOMatch!")
            builder.setNegativeButton("OK"){ _, _ ->
            }
            builder.show()

        }
        imageView = requireView().findViewById(R.id.profileView)
        val languageTxt = requireView().findViewById<TextView>(R.id.textViewLanguage)
        val recyclerView : RecyclerView = requireView().findViewById(R.id.editable_evaluation_recycler_view)
        requireView().findViewById<ImageButton>(R.id.imageButton).setOnClickListener { showPopup(requireView()) }




        viewModel.loadedData.observe(viewLifecycleOwner){
            if(it >= viewModel.expectedData){
                viewModel.userLiveData.observe(viewLifecycleOwner){

                    updateView(it)

                    viewModel.evaluationLiveData.observe(viewLifecycleOwner){
                        val adapter = SportEvaluationAdapter(viewModel.evaluationLiveData.value!!, viewModel.sports, requireContext(), viewModel)
                        recyclerView.adapter = adapter
                    }


                    languageTxt.text = viewModel.userLiveData.value!!.languages.joinToString(" ")

                    languageTxt.setOnClickListener {
                        val builder = AlertDialog.Builder(requireContext())
                        //setTitle
                        builder.setTitle("Select languages")
                        var languageArray = viewModel.languages.toTypedArray()
                        var checkedArray = languageArray.map { viewModel.userLiveData.value!!.languages.contains(it) }.toBooleanArray()
                        //set multichoice
                        builder.setMultiChoiceItems(languageArray, checkedArray) { _, which, isChecked ->
                            // Update the current focused item's checked status
                            checkedArray[which] = isChecked
                        }

                        // Set the positive/yes button click listener
                        builder.setPositiveButton("OK") { _, _ ->
                            var languageToBeUdated: List<String> = emptyList()
                            for ((index, bool) in checkedArray.iterator().withIndex()) {
                                if (bool) {
                                    languageToBeUdated = languageToBeUdated + languageArray[index]
                                }
                            }
                            val userToBeModified = viewModel.userLiveData.value
                            userToBeModified!!.languages = languageToBeUdated
                            languageTxt.text = languageToBeUdated.joinToString(" ")
                        }


                        // Set the neutral/cancel button click listener
                        builder.setNeutralButton("Cancel") { _, _ ->
                            // Do something when click the neutral button
                        }
                        val dialog = builder.create()
                        // Display the alert dialog on interface
                        dialog.show()
                    }
                }
            }
        }




        //ADD TO MAIN ACTIVITY
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || requireContext().checkSelfPermission(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_DENIED
            ) {
                val permission = arrayOf<String>(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permission, 112)
            }
        }

        val backCallback = object : OnBackPressedCallback(
            true
        ) {
            override fun handleOnBackPressed() {
                if(arguments?.getString("newUser")!=null) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setMessage("Please insert your data to sign up in GOMatch!")
                    builder.setNegativeButton("OK"){ _, _ ->
                    }
                    builder.show()
                }
                else{
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backCallback
        )

    }




    private fun updateView(p: FirebaseUser) {
        requireView().findViewById<EditText>(R.id.textViewName).setText(p.fullname)
        requireView().findViewById<EditText>(R.id.textViewNickName).setText(p.nickname)
        requireView().findViewById<EditText>(R.id.textViewPhone).setText(p.tel)
        requireView().findViewById<EditText>(R.id.editTextAge).setText(p.birthDate)
        requireView().findViewById<EditText>(R.id.textViewBio).setText(p.bio)
        if(viewModel.imageUri.toString().contains("google")){
            imageView.setImageURI(null)
            Glide.with(this)
                .load(viewModel.imageUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView)
        }
        else{
            imageView.setImageURI(null)
            imageView.setImageURI(viewModel.imageUri)
        }
    }


    fun showPopup(v: View) {
        val popup = PopupMenu(requireContext(), v.findViewById(R.id.imageButton))
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.photo_menu, popup.menu)

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.fromGallery -> {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryActivityResultLauncher.launch(galleryIntent)
                    true
                }
                R.id.takePicture -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || requireContext().checkSelfPermission(
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            == PackageManager.PERMISSION_DENIED
                        ) {
                            val permission = arrayOf<String>(
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                            requestPermissions(permission, 112)
                        } else {
                            openCamera()
                        }
                    } else {
                        openCamera()
                    }
                    true
                }
                else -> true
            }
        }

        popup.show()
    }


    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        viewModel.imageUri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.imageUri!!)
        cameraActivityResultLauncher.launch(cameraIntent)
    }


    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu,inflater)
        val inflater: MenuInflater = inflater
        inflater.inflate(R.menu.save_menu, menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.mainPage -> {
                try {
                    LocalDate.parse(
                        requireView().findViewById<EditText>(R.id.editTextAge).text.toString(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    )
                }
                catch (e: DateTimeParseException){
                    requireView().findViewById<EditText>(R.id.editTextAge).setError("Invalid date format! Expected: dd/mm/yyyy")
                    return true
                }

                if (LocalDate.parse(
                        requireView().findViewById<EditText>(R.id.editTextAge).text.toString(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    ).compareTo(LocalDate.now()) > 0){
                    requireView().findViewById<EditText>(R.id.editTextAge).setError("Are you from the future?")
                    return true
                }

                if (Period.between(
                            LocalDate.parse(requireView().findViewById<EditText>(R.id.editTextAge).text.toString(), DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            LocalDate.now()).years > 100){
                    requireView().findViewById<EditText>(R.id.editTextAge).setError("You could be dead!")
                    return true
                }

                if (LocalDate.parse(
                        requireView().findViewById<EditText>(R.id.editTextAge).text.toString(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    ).compareTo(LocalDate.now()) > 0){
                    requireView().findViewById<EditText>(R.id.editTextAge).setError("Are you from the future?")
                    return true
                }

                if (requireView().findViewById<EditText>(R.id.textViewName).text.trim().isEmpty()){
                    requireView().findViewById<EditText>(R.id.textViewName).setError("Please insert your name")
                    return true
                }

                if (requireView().findViewById<TextView>(R.id.textViewLanguage).text.isEmpty()){
                    requireView().findViewById<EditText>(R.id.textViewLanguage).setError("Please insert at least one Language!")
                    return true
                }

                if(!PhoneNumberUtils.isGlobalPhoneNumber(requireView().findViewById<EditText>(R.id.textViewPhone).text.toString())){
                    requireView().findViewById<EditText>(R.id.textViewPhone).setError("Invalid phone number!")
                    return true
                }

                viewModel.userLiveData.value!!.bio = requireView().findViewById<EditText>(R.id.textViewBio).text.toString()
                viewModel.userLiveData.value!!.birthDate = requireView().findViewById<EditText>(R.id.editTextAge).text.toString()
                viewModel.userLiveData.value!!.fullname = requireView().findViewById<EditText>(R.id.textViewName).text.toString()
                viewModel.userLiveData.value!!.nickname = requireView().findViewById<EditText>(R.id.textViewNickName).text.toString()
                viewModel.userLiveData.value!!.tel = requireView().findViewById<EditText>(R.id.textViewPhone).text.toString()

                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.updateUser()
                    findNavController().popBackStack()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
