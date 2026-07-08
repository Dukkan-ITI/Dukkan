package com.dukkan.chatbot.view

import com.dukkan.chatbot.R

import androidx.compose.foundation.background
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dukkan.chatbot.components.ChatInputBar
import com.dukkan.chatbot.components.MessageBubble
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
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        onProductClick = onProductClick,
                        onFavoriteClick = onFavoriteClick
                    )
                }

                if (isLoading) {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            if (messages.isEmpty() && !isLoading) {
                Text(
                    text = stringResource(R.string.chatbot_ready),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        ChatInputBar(
            onSend = onSendMessage,
            isLoading = isLoading
        )
    }
}