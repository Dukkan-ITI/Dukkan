package com.dukkan.chatbot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.FavoriteProduct
import com.dukkan.domain.model.SearchProduct
import com.dukkan.domain.model.chatbot.ChatMessage
import com.dukkan.domain.repository.ChatbotRepository
import com.dukkan.domain.usecase.favorite.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatEvent {
    data class NavigateToProduct(val productId: String) : ChatEvent
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatbotRepository: ChatbotRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = Channel<ChatEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add user message to UI
        val userMessage = ChatMessage(text = text, isFromUser = true)
        _messages.update { it + userMessage }

        // 2. Fetch bot response
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = chatbotRepository.sendMessage(text)
            
            result.onSuccess { botMessage ->
                _messages.update { it + botMessage }
            }.onFailure {
                val errorMessage = ChatMessage(
                    text = "Sorry, I couldn't process that request.", 
                    isFromUser = false
                )
                _messages.update { it + errorMessage }
            }
            
            _isLoading.value = false
        }
    }

    fun onProductClick(productId: String) {
        viewModelScope.launch {
            _events.send(ChatEvent.NavigateToProduct(productId))
        }
    }

    fun onFavoriteClick(product: SearchProduct) {
        viewModelScope.launch {
            val favoriteProduct = FavoriteProduct(
                id = product.id,
                title = product.title,
                imageUrl = product.imageUrl.orEmpty(),
                price = product.price.amount.toString(),
                currencyCode = product.price.currencyCode,
                rating = product.averageRating,
                reviewCount = product.reviewCount
            )
            // Note: Since we don't have isFavorite state in SearchProduct yet, 
            // we assume it's false for the toggle for now. 
            // In a real app, we would observe the favorite state of each product.
            toggleFavoriteUseCase(favoriteProduct, false)
        }
    }
}
