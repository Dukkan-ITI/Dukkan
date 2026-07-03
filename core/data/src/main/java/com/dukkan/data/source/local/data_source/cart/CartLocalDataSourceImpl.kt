package com.dukkan.data.source.local.data_source.cart

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dukkan.data.source.local.LocalConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = LocalConstants.CART_PREFERENCES_NAME)

class CartLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : CartLocalDataSource {

    private val CART_ID_KEY = stringPreferencesKey(LocalConstants.CART_ID_KEY_NAME)

    override suspend fun saveCartId(cartId: String) {
        context.dataStore.edit { preferences ->
            preferences[CART_ID_KEY] = cartId
        }
    }

    override suspend fun getCartId(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[CART_ID_KEY]
        }.first()
    }

    override suspend fun deleteCartId() {
        context.dataStore.edit { preferences ->
            preferences.remove(CART_ID_KEY)
        }
    }
}
