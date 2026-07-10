package com.dukkan.chatbot.view

import com.dukkan.chatbot.R

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.chatbot.components.BotAvatar
import com.dukkan.chatbot.components.ChatInputBar
import com.dukkan.chatbot.components.MessageBubble
import com.dukkan.chatbot.components.TypingIndicator
import com.dukkan.chatbot.viewModel.ChatEvent
import com.dukkan.chatbot.viewModel.ChatViewModel
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.model.chatbot.ChatMessage

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateToProduct: (String) -> Unit = {}
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatEvent.NavigateToProduct -> onNavigateToProduct(event.productId)
            }
        }
    }

    ChatContent(
        messages = messages,
        isLoading = isLoading,
        onSendMessage = { text -> viewModel.sendMessage(text) },
        onProductClick = viewModel::onProductClick,
        onFavoriteClick = viewModel::onFavoriteClick
    )
}

@Composable
fun ChatContent(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onFavoriteClick: (SearchProduct) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onProductClick = onProductClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }

                if (isLoading) {
                    item(key = "typing-indicator") {
                        TypingIndicator()
                    }
                }
            }

            if (messages.isEmpty() && !isLoading) {
                WelcomeState(onSuggestionClick = onSendMessage)
            }
        }

        ChatInputBar(
            onSend = onSendMessage,
            isLoading = isLoading
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeState(
    onSuggestionClick: (String) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    // Gentle breathing on the avatar so the greeting feels alive.
    val infiniteTransition = rememberInfiniteTransition(label = "WelcomeTransition")
    val avatarScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WelcomeAvatarScale"
    )

    // One-shot fade + rise when the empty state appears.
    val entered = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    AnimatedVisibility(
        visibleState = entered,
        enter = fadeIn(animationSpec = tween(500)) +
            slideInVertically(
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 4 }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.scale(avatarScale * 2f)) {
                BotAvatar()
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.chatbot_greeting_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    brush = Brush.linearGradient(colors = listOf(primary, tertiary)),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.chatbot_greeting_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    stringResource(R.string.chatbot_suggestion_deals),
                    stringResource(R.string.chatbot_suggestion_orders),
                    stringResource(R.string.chatbot_suggestion_gift)
                ).forEach { suggestion ->
                    SuggestionChip(text = suggestion, onClick = { onSuggestionClick(suggestion) })
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}