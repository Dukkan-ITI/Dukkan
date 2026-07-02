package com.msayeh.domain.model

/**
 * How the app decides its light/dark appearance.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * Currencies the user can choose. Shopify's Storefront API derives the presentment
 * currency from the buyer's country via the `@inContext(country:)` directive, so each
 * currency is mapped to a representative [countryCode].
 */
enum class AppCurrency(val code: String, val countryCode: String) {
    USD("USD", "US"),
    GBP("EGP", "EG"),
}

/**
 * App languages. [tag] is a BCP-47 tag used for the per-app locale, while [languageCode]
 * matches Shopify's `LanguageCode` enum used in the `@inContext(language:)` directive.
 */
enum class AppLanguage(val tag: String, val languageCode: String) {
    ENGLISH("en", "EN"),
    ARABIC("ar", "AR"),
}
