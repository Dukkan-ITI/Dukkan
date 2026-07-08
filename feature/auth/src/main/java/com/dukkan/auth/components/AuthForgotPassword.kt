package com.dukkan.auth.components


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dukkan.auth.R

@Composable
fun AuthForgotPasswordScreen(
    email: String,
    isLoading: Boolean,
    emailError: String?,
    isEmailSent: Boolean,
    onEmailChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackToLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(50.dp))

        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        val logoRes = if (isRtl) {
            com.dukkan.design_system.R.drawable.logo_ar
        } else {
            com.dukkan.design_system.R.drawable.logo_en
        }

        Image(
            painter = painterResource(id = logoRes),
            contentDescription = null,
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Fit
        )


        Column(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = isEmailSent,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) using SizeTransform(
                        clip = false
                    )
                },
                label = "ForgotPasswordTitleAnimation"
            ) { sent ->
                Text(
                    text = stringResource(
                        if (sent) R.string.auth_forgot_password_sent_title else R.string.auth_forgot_password_title
                    ),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        letterSpacing = (-1).sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = if (isEmailSent) {
                    stringResource(R.string.auth_forgot_password_sent_subtitle, email)
                } else {
                    stringResource(R.string.auth_forgot_password_subtitle)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                lineHeight = 24.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = !isEmailSent) {
            Column(horizontalAlignment = Alignment.Start) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = .12f))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEmailSent) Icons.Filled.MarkEmailRead else Icons.Filled.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(28.dp))

                AuthTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    placeholder = stringResource(R.string.auth_forgot_password_email_placeholder),
                    errorMessage = emailError,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                )

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = onSendClick,
                    enabled = !isLoading && email.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.auth_forgot_password_send_button),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        TextButton(
            onClick = onBackToLoginClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(R.string.auth_forgot_password_back_button),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}