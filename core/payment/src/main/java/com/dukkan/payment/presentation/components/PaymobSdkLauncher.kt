package com.dukkan.payment.presentation.components

import androidx.appcompat.app.AppCompatActivity

internal data class PaymobThemeColors(
    val primary: Int,
    val onPrimary: Int,
    val surface: Int,
    val onSurface: Int,
    val outline: Int,
    val error: Int,
)

internal class PaymobSdkLauncher(
    private val activity: AppCompatActivity,
    private val colors: PaymobThemeColors,
    private val onFinished: () -> Unit,
) : com.paymob.paymob_sdk.ui.PaymobSdkListener {

    fun launch(clientSecret: String, publicKey: String) {
        timber.log.Timber.d("PAYMOB_LAUNCH: clientSecret=%s, publicKey=%s", clientSecret, publicKey)
        com.paymob.paymob_sdk.PaymobSdk.Builder(
            context           = activity,
            clientSecret      = clientSecret,
            publicKey         = publicKey,
            paymobSdkListener = this,
        )
            .setButtonBackgroundColor(colors.primary)
            .setButtonTextColor(colors.onPrimary)
            .build()
            .start()
    }

    override fun onSuccess(payResponse: java.util.HashMap<String, String?>) {
        onFinished()
    }

    override fun onFailure(msg: String?) {
        onFinished()
    }

    override fun onPending() {
        onFinished()
    }
}
