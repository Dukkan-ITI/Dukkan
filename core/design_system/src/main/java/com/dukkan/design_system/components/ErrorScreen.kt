package com.dukkan.design_system.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dukkan.design_system.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dukkan.design_system.theme.AppTheme

/**
 * A full-screen, centered error state with an optional retry action.
 *
 * @param message The error message to display to the user.
 * @param modifier Modifier applied to the root container.
 * @param onRetry Optional callback. When provided, a "Retry" button is shown.
 * @param retryText Label for the retry button.
 */
@Composable
fun ErrorScreen(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    retryText: String = stringResource(id = R.string.retry_btn),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        if (onRetry != null) {
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(text = retryText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorScreenPreview() {
    AppTheme {
        ErrorScreen(
            message = stringResource(id = R.string.error_message_default),
            onRetry = {},
        )
    }
}
