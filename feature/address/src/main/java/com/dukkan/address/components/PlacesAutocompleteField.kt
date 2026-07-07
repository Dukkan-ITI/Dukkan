package com.dukkan.address.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dukkan.address.viewmodel.PlacesViewModel
import com.dukkan.domain.model.LocationDetail

@Composable
fun PlacesAutocompleteField(
    onPlaceSelected: (LocationDetail) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search address...",
    viewModel: PlacesViewModel = hiltViewModel()
) {
    var query by remember { mutableStateOf("") }
    val predictions by viewModel.predictions.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        AddressTextField(
            value = query,
            onValueChange = { 
                query = it
                viewModel.searchPlaces(it)
            },
            placeholder = placeholder,
            imeAction = ImeAction.Search,
            keyboardActions = KeyboardActions(
                onSearch = { viewModel.searchPlaces(query) }
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
                                    onPlaceSelected(prediction)
                                    query = prediction.name
                                    viewModel.clearPredictions()
                                }
                                .padding(16.dp)
                        ) {
                            Text(
                                text = prediction.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = prediction.address,
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
