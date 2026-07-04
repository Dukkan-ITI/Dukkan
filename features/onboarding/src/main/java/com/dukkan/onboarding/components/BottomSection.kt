package com.dukkan.onboarding.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomSection(
    modifier: Modifier = Modifier,
    isLastPage: Boolean,
    onContinue: () -> Unit,
    onSignInClick: () -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 40.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ContinueButton(
            isLastPage = isLastPage,
            onClick = onContinue
        )

        Spacer(modifier = Modifier.height(24.dp))

        BottomLoginSection(
            onSignInClick = onSignInClick
        )
    }
}
