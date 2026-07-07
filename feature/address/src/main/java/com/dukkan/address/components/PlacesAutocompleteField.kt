package com.dukkan.address.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun PlacesAutocompleteField(
    onPlaceSelected: (Place) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search address...",
) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val performSearch: (String) -> Unit = { searchQuery ->
        searchJob?.cancel()
        if (searchQuery.isNotEmpty()) {
            searchJob = coroutineScope.launch {
                delay(500) // Debounce
                isSearching = true
                val request = FindAutocompletePredictionsRequest.builder()
                    .setQuery(searchQuery)
                    .build()
                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        predictions = response.autocompletePredictions
                        isSearching = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("PlacesAutocomplete", "Error finding predictions", e)
                        predictions = emptyList()
                        isSearching = false
                    }
            }
        } else {
            predictions = emptyList()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        AddressTextField(
            value = query,
            onValueChange = { 
                query = it
                performSearch(it)
            },
            placeholder = placeholder,
            imeAction = ImeAction.Search,
            keyboardActions = KeyboardActions(
                onSearch = { performSearch(query) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (predictions.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shadowElevation = 4.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(predictions) { prediction ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val placeFields = listOf(
                                        Place.Field.ID, 
                                        Place.Field.NAME, 
                                        Place.Field.LAT_LNG, 
                                        Place.Field.ADDRESS_COMPONENTS, 
                                        Place.Field.ADDRESS
                                    )
                                    val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()
                                    placesClient.fetchPlace(request)
                                        .addOnSuccessListener { response ->
                                            onPlaceSelected(response.place)
                                            query = response.place.address ?: response.place.name ?: ""
                                            predictions = emptyList()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("PlacesAutocomplete", "Error fetching place details", e)
                                        }
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = prediction.getPrimaryText(null).toString(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = prediction.getSecondaryText(null).toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
