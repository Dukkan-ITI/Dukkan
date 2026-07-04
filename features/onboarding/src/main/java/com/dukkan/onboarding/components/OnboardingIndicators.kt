package com.dukkan.onboarding.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingIndicators(
    modifier: Modifier = Modifier,
    currentPage: Int,
    pageCount: Int
) {

    Row(
        modifier = modifier
    ) {

        repeat(pageCount) { index ->

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index == currentPage)
                            Color.White
                        else
                            Color.White.copy(alpha = .3f)
                    )
            )

        }

    }

}
