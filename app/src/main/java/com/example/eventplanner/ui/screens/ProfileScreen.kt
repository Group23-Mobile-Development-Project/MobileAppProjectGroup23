package com.example.eventplanner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventplanner.utils.AuthUtils

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        AuthUtils.fetchUserProfile(
            onSuccess = { profileData ->
                email = profileData?.get("email") as? String ?: ""
                name = profileData?.get("name") as? String ?: ""
                isLoading = false
            },
            onFailure = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Screen", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, enabled = false)
        Spacer(modifier = Modifier.height(8.dp))

        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                AuthUtils.updateUserProfile(
                    updatedData = mapOf("name" to name),
                    onSuccess = {
                        Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    },
                    onFailure = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
                )
            },
            enabled = !isLoading
        ) {
            Text(text = "Update Profile")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            AuthUtils.logout {
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }) {
            Text(text = "Logout")
        }
    }
}
