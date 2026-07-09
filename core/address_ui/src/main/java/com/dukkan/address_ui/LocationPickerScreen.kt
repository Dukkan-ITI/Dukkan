package com.dukkan.address_ui

import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dukkan.design_system.components.PrimaryButton
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationPickerScreen(
    onLocationSelected: (LatLng) -> Unit,
    onCancel: () -> Unit,
) {
    // Default location (e.g., Cairo, or could be passed in)
    val defaultLocation = LatLng(30.0444, 31.2357)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(compassEnabled = true, zoomControlsEnabled = false)
        )

        // Center Marker Overlay
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Selected Location",
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .offset(y = (-24).dp), // Offset so the pin points exactly at the center
            tint = MaterialTheme.colorScheme.primary
        )

        // Bottom Bar
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.move_map_delivery),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    PrimaryButton(
                        text = stringResource(R.string.confirm_location),
                        onClick = { onLocationSelected(cameraPositionState.position.target) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
