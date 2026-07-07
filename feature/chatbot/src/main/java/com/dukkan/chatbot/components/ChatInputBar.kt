package com.dukkan.chatbot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputBar(
    onSend: (String) -> Unit,
    isLoading: Boolean
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding() // Prevents overlapping with the system navigation gesture bar
            .imePadding()            // Smoothly shifts the entire bar upward when the soft keyboard appears
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask about products...") },
            shape = RoundedCornerShape(24.dp),
            maxLines = 3
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                onSend(text)
                text = ""
            },
            enabled = text.isNotBlank() && !isLoading,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}