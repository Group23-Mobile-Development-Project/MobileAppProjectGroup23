package com.example.eventplanner.ui.components

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEventButton(onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    var selectedDate by remember { mutableStateOf("") }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val formattedDate = "$selectedMonth/${selectedDay + 1}/$selectedYear"
                selectedDate = formattedDate
                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        )
    }

    FloatingActionButton(onClick = { datePickerDialog.show() }) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
    }

    // This will display the selected date below the button (optional, for demonstration)
    if (selectedDate.isNotEmpty()) {
        Text("Selected Date: $selectedDate")
    }
}
