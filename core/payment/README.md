# 💳 Payment Module Manual

Welcome to the `core:payment` module! This module provides a complete, modern, and animated checkout experience, including Address Management and Payment Processing (Cash & Online via Paymob).

This quick manual explains how to integrate, configure, and handle the results of this feature.

## 🚀 1. How to Launch the Feature
To launch the payment flow, simply navigate to the `paymentNavGraph` in your NavHost and provide the user's `CartSummary`. 

The entry point expects you to handle the `onPaymentResult` callback, which is how the module hands control back to you when the user finishes.

```kotlin
// In your NavHost
paymentNavGraph(
    cartSummary = currentCartSummary, // Pass the cart details
    onPaymentResult = { result ->
        when (result) {
            is PaymentResult.Success -> {
                // The user successfully paid (or chose Cash).
                // DO YOUR FINAL ORDER COMPLETION LOGIC HERE!
                val method = result.paymentMethod // "CASH" or "ONLINE"
                val orderId = result.orderId
            }
            is PaymentResult.Pending -> {
                // Online payment is processing
            }
            is PaymentResult.Failure -> {
                // Payment failed, show a toast or error UI
            }
            is PaymentResult.Cancelled -> {
                // User backed out of the checkout screen
            }
        }
    }
)
```

## 🔄 2. The Two Payment Flows

### 💵 Flow A: Cash on Delivery
When the user selects **Cash**:
1. They confirm their address.
2. The module intercepts the success state and **instantly bypasses the receipt screen**.
3. It immediately fires `PaymentResult.Success` with `paymentMethod = "CASH"`.
4. **Your job:** When you receive this in `onPaymentResult`, you must trigger the backend API to officially "Complete" the order.

### 💳 Flow B: Online Payment (Paymob)
When the user selects **Online**:
1. **Auto-Conversion:** Paymob strictly requires Egyptian Pounds (`EGP`). If the user's cart is in `USD`, the module automatically fetches the live exchange rate (via Open Exchange Rates) and converts the total to `EGP` seamlessly.
2. **SDK Launch:** It launches the native Paymob Android SDK.
3. **Receipt Overlay:** Upon finishing, the module displays a beautiful, animated receipt (Success, Pending, or Failure).
4. **Handoff:** When the user taps "Done" on the receipt, it fires `PaymentResult.Success` with `paymentMethod = "ONLINE"`.

## 🔐 3. Security & API Keys
We NEVER hardcode API keys in the source code. The keys for Paymob are injected securely during compilation using `BuildConfig`.

**For your local machine to build the project**, you must add the following lines to your global Gradle properties file (`~/.gradle/gradle.properties` on Mac/Linux or `C:\Users\YourName\.gradle\gradle.properties` on Windows):

```properties
paymobPublicKey=your_public_key_here
paymobClientSecret=your_secret_key_here
```
If you don't do this, the project will use placeholder strings and the Paymob integration will crash!

## 🎨 4. UI Architecture
- **CheckoutScreen**: The main composable orchestrating the UI.
- **AddressEditSheet**: A premium, 2-column bottom sheet for editing delivery details.
- **PaymentResultOverlay**: A fullscreen animated receipt that handles the Paymob visual callbacks. 

If you ever need to add a new Payment Method (like PayPal), simply add a new `MethodTile` in the UI and wire its click to a new intention use-case!

## ?? 5. Paymob SDK Integration
The Paymob SDK is distributed as an .aar file. Because committing binary files directly to the repository bloats the git history, paymob-sdk.aar is added to .gitignore.

During Continuous Integration (CI), the AAR is automatically downloaded via a curl step in .github/workflows/android-pr-check.yml.

**For Local Development**: You must download the paymob-sdk.aar from the Paymob developer portal (https://docs.paymob.com/docs/android-sdk) and manually place it in the pp/libs/ directory. If it is missing, your local build will fail!

