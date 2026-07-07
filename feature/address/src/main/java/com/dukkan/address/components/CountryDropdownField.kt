package com.dukkan.address.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private object CountryCatalog {
    @Volatile
    var countries: List<String>? = null

    fun compute(): List<String> = Locale.getISOCountries()
        .map { Locale.Builder().setRegion(it).build().displayCountry }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()
}

@Composable
fun CountryDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    var countries by remember { mutableStateOf(CountryCatalog.countries.orEmpty()) }

    LaunchedEffect(Unit) {
        if (CountryCatalog.countries == null) {
            val computed = withContext(Dispatchers.Default) { CountryCatalog.compute() }
            CountryCatalog.countries = computed
            countries = computed
        }
    }

    val shape = RoundedCornerShape(14.dp)
    val backgroundColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        expanded -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, shape)
                .border(
                    width = if (expanded) 2.dp else 1.dp,
                    color = borderColor,
                    shape = shape,
                )
                .clip(shape)
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 17.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value.ifBlank { placeholder },
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    value.isBlank() -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = shape,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .heightIn(max = 320.dp),
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = { Text(country) },
                    onClick = {
                        onValueChange(country)
                        expanded = false
                    },
                )
            }
        }
    }
}
