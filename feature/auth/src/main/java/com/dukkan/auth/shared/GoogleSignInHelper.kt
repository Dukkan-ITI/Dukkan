package com.dukkan.auth.shared

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential


class GoogleSignInHelper {

    companion object {
        const val WEB_CLIENT_ID = "187682081726-3uu2n467cah51k3g40e35o1cv4ne0the.apps.googleusercontent.com"
    }
    suspend fun signIn(activityContext: Context): String {
        val credentialManager = CredentialManager.create(activityContext)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            context = activityContext,
            request = request,
        )

        val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        return googleCredential.idToken
    }
}
