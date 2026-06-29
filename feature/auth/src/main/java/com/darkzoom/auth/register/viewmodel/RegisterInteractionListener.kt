package com.darkzoom.auth.register.viewmodel

interface RegisterInteractionListener {
    fun onNameChanged(name: String)
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onRegisterClicked()
    fun onNavigateToLogin()
    fun onGoogleClicked()
    fun onGoogleIdTokenReceived(idToken: String)
    fun onGoogleSignInFailed(message: String)
    fun onContinueAsGuest()
}