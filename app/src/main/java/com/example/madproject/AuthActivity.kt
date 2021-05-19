package com.example.madproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.lib.Requests
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class AuthActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = Firebase.auth

        if (mAuth.currentUser == null) {
            setContentView(R.layout.auth_activity)

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInButton = findViewById<Button>(R.id.sbtn)
            signInButton.setOnClickListener {
                signIn()
            }

        } else {
            startMainActivity()
        }


    }

    private fun startMainActivity() {
        FirestoreRepository.auth = mAuth.currentUser!!
        FirestoreRepository().getUser().addSnapshotListener { value, error ->
            if (error == null) {
                var flag = false
                if (value != null && !value.exists()) {
                    flag = true
                }
                Log.d("test", "SetNavigation() from activity -> onCreate()")

                val intent = Intent(this, MainActivity::class.java).also {
                    it.putExtra("INTENT_NEED_REGISTRATION_EXTRA", flag)
                }

                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Firebase failure!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, Requests.RC_SIGN_IN.value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Requests.RC_SIGN_IN.value) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //this line is just for checking if authentication works or no
                    startMainActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show()
                }
            }
    }
}