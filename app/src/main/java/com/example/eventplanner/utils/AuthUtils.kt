package com.example.eventplanner.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object AuthUtils {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Function to log in an existing user
    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "Login failed")
                }
            }
    }

    // Function to register a new user
    fun signUpUser(email: String, password: String, name: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = hashMapOf(
                            "email" to email,
                            "name" to name
                        )
                        db.collection("users").document(user.uid).set(userData)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to save user data.") }
                    }
                } else {
                    onFailure(task.exception?.message ?: "Signup failed")
                }
            }
    }

    // Function to fetch user profile data
    fun fetchUserProfile(onSuccess: (Map<String, Any>?) -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        onSuccess(document.data)
                    } else {
                        onFailure("User profile not found.")
                    }
                }
                .addOnFailureListener { e ->
                    onFailure(e.message ?: "Failed to fetch user profile.")
                }
        } else {
            onFailure("No authenticated user found.")
        }
    }

    // Function to update user profile data
    fun updateUserProfile(updatedData: Map<String, Any>, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid).update(updatedData)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.message ?: "Failed to update profile.") }
        } else {
            onFailure("No authenticated user found.")
        }
    }

    // Function to logout the user
    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }
}
