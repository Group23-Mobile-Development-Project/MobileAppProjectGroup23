package com.example.eventplanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

object GoogleSignInUtils {

    private lateinit var googleSignInClient: GoogleSignInClient

    /**
     * Initializes the GoogleSignInClient with proper configuration
     */
    fun initGoogleSignInClient(context: Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    /**
     * Launches the Google Sign-In intent.
     */
    fun launchGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    /**
     * Handles the result from Google Sign-In and authenticates with Firebase.
     */
    fun handleSignInResult(
        context: Context,
        data: Intent?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(context, account, onSuccess, onFailure)
        } catch (e: ApiException) {
            onFailure("Google sign-in failed: ${e.message}")
        }
    }

    private fun firebaseAuthWithGoogle(
        context: Context,
        account: GoogleSignInAccount,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Firebase sign-in failed: ${task.exception?.message}")
                }
            }
    }

    /**
     * Optional: Call this to sign out the user from Google as well.
     */
    fun signOut(context: Context) {
        googleSignInClient.signOut()
    }
}
