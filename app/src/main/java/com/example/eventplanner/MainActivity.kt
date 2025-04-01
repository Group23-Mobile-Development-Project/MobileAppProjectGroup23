package com.example.eventplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.eventplanner.ui.navigation.AppNavGraph
import com.example.eventplanner.ui.theme.EventPlannerTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()  // Firebase Authentication instance

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Enables splash screen
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventPlannerTheme {
                var isSplashVisible by remember { mutableStateOf(true) }
                val navController = rememberNavController() // Initialize NavController

                // Simulate a loading delay for splash screen
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
