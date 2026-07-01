package com.dukkan.onboarding.components


import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TopBar(
    currentPage: Int,
    pageCount: Int,
    onSkip: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 48.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        OnboardingIndicators(
            modifier = Modifier.weight(1f),
            currentPage = currentPage,
            pageCount = pageCount
        )

        Spacer(modifier = Modifier.width(16.dp))

        SkipButton(
            visible = currentPage < pageCount - 1,
            onClick = onSkip
        )

    }

}