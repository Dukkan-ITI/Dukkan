package com.dukkan.search.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dukkan.search.R
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle.Default
) {
    var visibleText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        if (text.isEmpty()) {
            visibleText = ""
            return@LaunchedEffect
        }
        

        visibleText = ""
        for (i in text.indices) {
            visibleText = text.substring(0, i + 1)

            delay(15) 
        }
    }

    Text(
        text = visibleText,
        modifier = modifier,
        color = color,
        style = style
    )
}

@Composable
fun ClarificationPrompt(
    question: String?,
    answer: String,
    message: String?,
    isLoading: Boolean,
    isError: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = isLoading || !question.isNullOrBlank() || !message.isNullOrBlank()

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { 12 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) + fadeIn(animationSpec = tween(400)),
        exit = slideOutVertically(
            targetOffsetY = { 12 },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier.fillMaxWidth()
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "CardGlow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "CardGlowAlpha"
        )
        
        val gradientBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF4285F4).copy(alpha = glowAlpha),
                Color(0xFF9b72cb).copy(alpha = glowAlpha),
                Color(0xFFf4a261).copy(alpha = glowAlpha)
            )
        )

        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .border(
                    width = 1.dp,
                    brush = gradientBrush,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedAiIcon(isLoading = isLoading)
                    
                    Text(
                        text = stringResource(R.string.search_ai_assistant_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.search_clear),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                val visibleMessage = question ?: message
                if (!visibleMessage.isNullOrBlank()) {
                    TypewriterText(
                        text = visibleMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (isError) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.search_retry))
                    }
                } else if (!question.isNullOrBlank()) {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = onAnswerChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = {
                            Text(stringResource(R.string.search_ai_answer_placeholder))
                        }
                    )
                    Button(
                        onClick = onSubmitAnswer,
                        enabled = answer.isNotBlank(),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.search_ai_send_answer))
                    }
                }
            }
        }
    }
}
