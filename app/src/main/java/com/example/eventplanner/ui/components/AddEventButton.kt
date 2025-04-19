// AddEventButton.kt
package com.example.eventplanner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun AddEventButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
    }
}
