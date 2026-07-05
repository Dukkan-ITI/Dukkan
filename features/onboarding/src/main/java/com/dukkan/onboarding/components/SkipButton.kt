package com.dukkan.onboarding.components


import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dukkan.onboarding.R

@Composable
fun SkipButton(
    visible: Boolean,
    onClick: () -> Unit
) {

    Text(

        style = MaterialTheme.typography.bodyMedium,
        text = stringResource(id = R.string.skip),
        color = MaterialTheme.colorScheme.onPrimary ,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.clickable(
            enabled = visible
        ) {
            onClick()
        }
    )

}