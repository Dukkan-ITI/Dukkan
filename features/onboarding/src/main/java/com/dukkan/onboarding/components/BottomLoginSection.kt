package com.dukkan.onboarding.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.dukkan.onboarding.R

@Composable
fun BottomLoginSection(
    onSignInClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        Text(
            style = MaterialTheme.typography.bodyMedium,
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = .8f))) {
                    append(stringResource(id = R.string.already_shopping))
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,

                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(id = R.string.sign_in))
                }
            },
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                onSignInClick()

            }
        )

    }

}