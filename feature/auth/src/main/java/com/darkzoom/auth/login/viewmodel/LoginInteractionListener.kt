package com.darkzoom.auth.login.viewmodel

interface LoginInteractionListener {
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onLoginClicked()
    fun onNavigateToRegister()
    fun onGoogleClicked()
    fun onGoogleIdTokenReceived(idToken: String)
    fun onGoogleSignInFailed(message: String)
    fun onContinueAsGuest()
}
