package com.darkzoom.auth.login.viewmodel


interface LoginInteractionListener {
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onLoginClicked()
    fun onNavigateToRegister()
    fun onGoogleClicked()
    fun onAppleClicked()
    fun onContinueAsGuest()
}
