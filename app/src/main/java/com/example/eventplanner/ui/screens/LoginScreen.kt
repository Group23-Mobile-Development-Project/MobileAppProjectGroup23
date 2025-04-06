package com.example.eventplanner.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventplanner.GoogleSignInUtils
import com.example.eventplanner.utils.AuthUtils

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as Activity
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Initialize Google Sign-In client when the screen is created
    LaunchedEffect(Unit) {
        GoogleSignInUtils.initGoogleSignInClient(context)
    }

    // Handle Google Sign-In intent result
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        GoogleSignInUtils.handleSignInResult(
            context = context,
            data = data,
            onSuccess = {
                Toast.makeText(context, "Google Sign-In Success", Toast.LENGTH_SHORT).show()
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            },
            onFailure = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
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
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Email TextField
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password TextField
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = {
                isLoading = true
                AuthUtils.loginUser(
                    email = email,
                    password = password,
                    onSuccess = {
                        isLoading = false
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onFailure = { errorMessage ->
                        isLoading = false
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In Button
        Button(
            onClick = {
                GoogleSignInUtils.launchGoogleSignIn(googleLauncher)
            }
        ) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("signup") }) {
            Text(text = "Don't have an account? Sign up")
        }
    }
}
