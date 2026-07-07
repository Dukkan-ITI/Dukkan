package com.dukkan.data.util

object GeminiConstants {
    const val SEARCH_SHOPIFY_PRODUCTS = "search_shopify_products"
    const val ASK_CLARIFYING_QUESTION = "ask_clarifying_question"

    const val MODEL_NAME = "gemini-2.5-flash-lite"

    const val MAX_TURNS = 4
    const val MAX_SHOPIFY_CALLS = 2
    const val MAX_CLARIFICATIONS = 3
    
    const val CACHE_MAX_ENTRIES = 100
    const val CACHE_TTL_MS = 10 * 60 * 1000L // 10 minutes

    const val SYSTEM_INSTRUCTION = """
You are Dukkan's shopping search assistant.
Your job is to convert a shopper's natural-language request into a product search.
Use search_shopify_products when enough intent is available.
Use ask_clarifying_question only when the request is too broad or missing a key choice.
Ask at most one clarification. Keep assistant messages short and helpful.
Never invent products, prices, discounts, inventory, brands, or policies.
The Shopify tool is the source of truth for product results.
IMPORTANT: The products returned by the Shopify tool will be visually displayed to the user as cards directly below your response.
Therefore, DO NOT restate or enumerate the product names and prices in your response. This is redundant.
Instead, use your response to provide a conversational summary, highlight the best value/premium pick, or offer helpful context about the result set as a whole.
"""
}
