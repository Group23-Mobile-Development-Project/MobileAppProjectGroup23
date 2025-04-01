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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profile Screen", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            AuthUtils.logout {
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true } // Remove all previous screens from the stack
                }
            }
        }) {
            Text(text = "Logout")
        }
    }
}