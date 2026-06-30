package com.dukkan.auth.shared.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.auth.R
import com.example.design_system.theme.AppTheme

@Composable
fun AuthSocialButton(
    label: String,
    leadingText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingColor: Color = MaterialTheme.colorScheme.onBackground,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF4285F4),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = leadingText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                    color = leadingColor,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun AuthSocialRow(
    onGoogleClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGoogleLoading: Boolean = false,
) {
    AuthSocialButton(
        label = stringResource(R.string.auth_social_google),
        leadingText = "G",
        leadingColor = Color(0xFF4285F4),
        onClick = onGoogleClick,
        modifier = modifier.fillMaxWidth(),
        isLoading = isGoogleLoading,
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthSocialRowPreview() {
    AppTheme {
        AuthSocialRow(
            onGoogleClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthSocialRowLoadingPreview() {
    AppTheme {
        AuthSocialRow(
            onGoogleClick = {},
            modifier = Modifier.padding(16.dp),
            isGoogleLoading = true,
        )
    }
}