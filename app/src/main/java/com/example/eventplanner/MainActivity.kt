package com.example.eventplanner

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.eventplanner.ui.navigation.AppNavGraph
import com.example.eventplanner.ui.theme.EventPlannerTheme
import com.example.eventplanner.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Register permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel at app startup
        NotificationHelper.createNotificationChannel(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        setContent {
            EventPlannerTheme {
                var isSplashVisible by remember { mutableStateOf(true) }
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    delay(2000)
                    isSplashVisible = false
                }

                if (isSplashVisible) {
                    SplashScreen()
                } else {
                    MainScreen(navController)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted, request it
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                Log.d("MainActivity", "Requesting notification permission")
            } else {
                Log.d("MainActivity", "Notification permission already granted")
            }
        }
    }
}

@Composable
fun MainScreen(navController: androidx.navigation.NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val isUserLoggedIn = auth.currentUser != null

    // If user is already logged in, navigate to the home screen
    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("login")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AppNavGraph(
            navController = navController, // Pass NavController to Navigation Graph
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // Replace with your actual logo
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Event Planner",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Organize and manage your events effortlessly with us!")
        Spacer(modifier = Modifier.height(32.dp))
    }
}