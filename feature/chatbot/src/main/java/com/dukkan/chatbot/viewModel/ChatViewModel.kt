package com.dukkan.chatbot.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dukkan.domain.model.chatbot.ChatMessage
import com.dukkan.domain.repository.ChatbotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatbotRepository: ChatbotRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add user message to UI
        val userMessage = ChatMessage(text = text, isFromUser = true)
        _messages.update { it + userMessage }

        // 2. Fetch bot response
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = chatbotRepository.sendMessage(text)
            
            result.onSuccess { replyText ->
                val botMessage = ChatMessage(text = replyText, isFromUser = false)
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
}
