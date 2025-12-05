package com.example.stock.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Dropdown for selecting chart interval (daily, weekly, monthly).
 *
 * @param selected Currently selected interval (e.g., "1day")
 * @param onSelected Callback when interval is selected
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalDropDown(
    selected: String,
    onSelected: (String) -> Unit
) {
    // List of options
    val options = listOf("1day", "1week", "1month")
    // Dropdown expansion state
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        // Text field displaying the selected interval (read-only)
        TextField(
            value = intervalLabels[selected] ?: selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        // Dropdown menu body
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(intervalLabels[option] ?: option) },
                    onClick = {
                        onSelected(option) // Call callback on selection
                        expanded = false  // Close menu
                    }
                )
            }
        }
    }
}

// Map for interval label display
private val intervalLabels = mapOf(
    "1day" to "Daily",
    "1week" to "Weekly",
    "1month" to "Monthly"
)
