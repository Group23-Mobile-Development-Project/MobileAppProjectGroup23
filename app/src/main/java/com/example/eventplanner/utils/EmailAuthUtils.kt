package com.example.eventplanner.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

object EmailAuthUtils {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    fun loginUser(context: Context, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun registerUser(context: Context, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Signup successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
