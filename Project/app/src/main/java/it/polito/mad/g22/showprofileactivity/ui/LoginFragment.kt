package it.polito.mad.g22.showprofileactivity.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g22.showprofileactivity.R
import it.polito.mad.g22.showprofileactivity.SavedPreference
import it.polito.mad.g22.showprofileactivity.data.FirebaseUser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val firebaseDb = Firebase.firestore
    var userLiveData: MutableLiveData<FirebaseUser> = MutableLiveData<FirebaseUser>()
    fun getData (userEmail:String){

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

class LoginFragment  : Fragment(R.layout.login) {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide();
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        FirebaseApp.initializeApp(requireContext())
        // Configure Google Sign In inside onCreate mentod
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()// getting the value of gso inside the GoogleSigninClient

        mGoogleSignInClient= GoogleSignIn.getClient(requireActivity(),gso)// initialize the firebaseAuth variable firebaseAuth= FirebaseAuth.getInstance()

        firebaseAuth= FirebaseAuth.getInstance()

        requireView().findViewById<CardView>(R.id.SigIn).setOnClickListener{
            Toast.makeText(requireContext(),"Logging In",Toast.LENGTH_SHORT).show()
            signInGoogle()
        }
    }

    private  fun signInGoogle(){
        val signInIntent: Intent =mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    // handleResult() function -  this is where we update the UI after Google signin takes place
    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
    try {
        val account: GoogleSignInAccount? =completedTask.getResult(ApiException::class.java)
        if (account != null) {
            UpdateUI(account)

        }
    } catch (e:ApiException){
        Toast.makeText(requireContext(),e.toString(),Toast.LENGTH_SHORT).show()
    }
}

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            UpdateUI(account)
        }
    }
    private fun UpdateUI(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {task->
            if(task.isSuccessful) {
                SavedPreference.setEmail(requireContext(), account.email.toString())
                SavedPreference.setUsername(requireContext(), account.displayName.toString())
                val viewModel: LoginViewModel by viewModels()
                viewModel.getData(account.email.toString())
                viewModel.userLiveData.observe(viewLifecycleOwner) {
                    if (it == null) {
                        val bundle = Bundle()
                        bundle.putString("newUser", Json.encodeToString(true))
                        findNavController().navigate(R.id.editPageActivity2,bundle)
                    }
                    else {
                        findNavController().navigate(R.id.homePage)
                    }
                }
            }
        }
    }
}