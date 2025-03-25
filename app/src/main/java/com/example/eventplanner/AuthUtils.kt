package com.example.eventplanner

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

object AuthUtils {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signUpWithEmail(context: Context, email: String, password: String, onSuccess: () -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                    Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun loginWithEmail(context: Context, email: String, password: String, onSuccess: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}
