package com.dukkan.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dukkan.data.source.remote.dto.ShopifyCustomerTokenDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

private val Context.shopifyTokenDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "shopify_token_prefs"
)

interface ShopifyTokenStore {
    suspend fun saveToken(token: ShopifyCustomerTokenDto)
    suspend fun getToken(): ShopifyCustomerTokenDto?
    suspend fun clearToken()
}

class ShopifyTokenStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ShopifyTokenStore {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("shopify_access_token")
        private val KEY_EXPIRES_AT = stringPreferencesKey("shopify_expires_at")
    }

    override suspend fun saveToken(token: ShopifyCustomerTokenDto) {
        context.shopifyTokenDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token.accessToken
            prefs[KEY_EXPIRES_AT] = token.expiresAt
        }
    }

    override suspend fun getToken(): ShopifyCustomerTokenDto? {
        val prefs = context.shopifyTokenDataStore.data.firstOrNull() ?: return null
        val accessToken = prefs[KEY_ACCESS_TOKEN] ?: return null
        val expiresAt = prefs[KEY_EXPIRES_AT] ?: return null
        return ShopifyCustomerTokenDto(accessToken = accessToken, expiresAt = expiresAt)
    }

    override suspend fun clearToken() {
        context.shopifyTokenDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_EXPIRES_AT)
        }
    }
}
