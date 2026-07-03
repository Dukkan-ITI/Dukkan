package com.dukkan.design_system.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dukkan.design_system.R
import com.dukkan.design_system.theme.AppTheme

/**
 * A full-screen, centered placeholder for destinations that don't have a real
 * screen yet (e.g. Search, Profile). Displays a simple "coming soon" message.
 *
 * @param title Name of the destination, shown as "<title> — coming soon".
 * @param modifier Modifier applied to the root container.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = bottomBarSpace()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.coming_soon_title, title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    AppTheme {
        PlaceholderScreen(title = "Search")
    }
}
