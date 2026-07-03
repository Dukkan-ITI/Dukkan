package com.dukkan.data.source.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.couponDataStore: DataStore<Preferences> by preferencesDataStore(
    name = LocalConstants.COUPON_NAME
)

interface CouponStore {
    val savedCoupon: Flow<String?>
    suspend fun saveCoupon(couponCode: String)
    suspend fun clearCoupon()
}

class CouponStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : CouponStore {

    companion object {
        private val KEY_COUPON_CODE = stringPreferencesKey(LocalConstants.COUPON_CODE)
    }

    override val savedCoupon: Flow<String?> =
        context.couponDataStore.data.map { it[KEY_COUPON_CODE] }

    override suspend fun saveCoupon(couponCode: String) {
        context.couponDataStore.edit { it[KEY_COUPON_CODE] = couponCode }
    }

    override suspend fun clearCoupon() {
        context.couponDataStore.edit { it.remove(KEY_COUPON_CODE) }
    }
}
