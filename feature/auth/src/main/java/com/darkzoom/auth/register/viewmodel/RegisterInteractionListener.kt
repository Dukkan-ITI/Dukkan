package com.darkzoom.auth.register.viewmodel


interface RegisterInteractionListener {
    fun onNameChanged(name: String)
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onRegisterClicked()
    fun onNavigateToLogin()
    fun onGoogleClicked()
    fun onAppleClicked()
    fun onContinueAsGuest()
}