package com.dukkan.auth.shared.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.design_system.theme.AppTheme

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    errorMessage: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val hasError = errorMessage != null
    val borderColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    val shape = RoundedCornerShape(14.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            interactionSource = interactionSource,
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction,
            ),
            keyboardActions = keyboardActions,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, shape)
                        .border(1.5.dp, borderColor, shape)
                        .padding(horizontal = 17.dp, vertical = 15.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            },
        )

        AnimatedVisibility(
            visible = hasError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.5.sp),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Empty")
@Composable
private fun AuthTextFieldEmptyPreview() {
    AppTheme {
        AuthTextField(
            value = "",
            onValueChange = {},
            placeholder = "Email address",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Filled")
@Composable
private fun AuthTextFieldFilledPreview() {
    AppTheme {
        AuthTextField(
            value = "user@example.com",
            onValueChange = {},
            placeholder = "Email address",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun AuthTextFieldErrorPreview() {
    AppTheme {
        AuthTextField(
            value = "bad-email",
            onValueChange = {},
            placeholder = "Email address",
            errorMessage = "Enter a valid email address",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, name = "Error — dark")
@Composable
private fun AuthTextFieldErrorDarkPreview() {
    AppTheme(darkTheme = true) {
        AuthTextField(
            value = "bad-email",
            onValueChange = {},
            placeholder = "Email address",
            errorMessage = "Enter a valid email address",
            modifier = Modifier.padding(16.dp),
        )
    }
}