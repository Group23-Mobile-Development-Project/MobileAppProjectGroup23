package com.example.eventplanner.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eventplanner.R

data class Event(val title: String, val date: String, val rsvpStatus: String, val imageRes: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val events = listOf(
        Event("Jazz Night", "Oct 14, 2023", "RSVP: Yes", R.drawable.jazz_night),
        Event("Art Exhibit", "Oct 20, 2023", "RSVP: No", R.drawable.art_exhibit),
        Event("Tech Conference", "Nov 5, 2023", "RSVP: Yes", R.drawable.tech_conference),
        Event("Food Festival", "Nov 10, 2023", "RSVP: Pending", R.drawable.food_festival),
        Event("Book Launch", "Nov 18, 2023", "RSVP: No", R.drawable.book_launch)
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Home") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(events) { event ->
                EventItem(event)
            }
        }
    }
}

@Composable
fun EventItem(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* Handle event click */ },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = painterResource(id = event.imageRes),
                contentDescription = event.title,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = event.date, fontSize = 14.sp, color = Color.Gray)
                Text(text = event.rsvpStatus, fontSize = 14.sp, color = Color.Blue)
            }
        }
    }
}
