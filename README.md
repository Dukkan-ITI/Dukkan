# Dukkan

**Dukkan** is a production-grade, multi-module Android e-commerce app built entirely in **Jetpack
Compose** and backed by the **Shopify Storefront GraphQL API**. It pairs a strict clean-architecture
module graph with real-world commerce concerns — Firebase auth, offline caching, Paymob payments,
Google Maps address selection, and an AI shopping assistant that routes across multiple LLM
providers.

## Architecture

Dukkan follows **Clean Architecture** with **MVVM** at the presentation layer, split across
independent Gradle modules. Dependencies always point inward toward `:core:domain`; features never
depend on each other or on `:core:data`.

| Layer / Pattern             | Description                                                                                                                                                                                     |
|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Clean Architecture**      | Three module groups — `core`, `feature`, `app` — with dependencies pointing inward to a pure-Kotlin domain core.                                                                                |
| **`:core:domain`**          | Framework-agnostic business layer: domain models, repository **interfaces**, and single-responsibility use cases. Depends only on `javax.inject`.                                               |
| **`:core:data`**            | Repository implementations over three coexisting data sources — Apollo/Shopify (remote), Firebase (auth + Firestore), and Room (local cache) — with mappers converting each into domain models. |
| **MVVM**                    | Each feature exposes a single immutable `*State` via `StateFlow` from a `@HiltViewModel`, consumed by stateless Compose screens.                                                                |
| **Modular navigation**      | A central `Screen` sealed class in `:core:navigation` is the single source of truth for routes; both the app `NavHost` and feature ViewModels (via `SavedStateHandle`) reference it.            |
| **Convention plugins**      | Shared Gradle setup lives in `build-logic/` as composite-build plugins (`dukkan.feature`, `dukkan.hilt`), keeping every feature module's build script minimal.                                  |
| **Multi-provider AI agent** | `:core:ai_agent` abstracts LLM tasks behind a provider/orchestrator/router contract, switching between Firebase AI and a local Ollama provider per task.                                        |

## Tech Stack

| Technology                                    | Description                                                                                                        |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| **Kotlin 2.0**                                | Primary language across all modules, including coroutines + `StateFlow` for async and reactive state.              |
| **Jetpack Compose** (Material 3)              | Declarative UI toolkit for the entire app; shared theme, typography, and components live in `:core:design_system`. |
| **Apollo Kotlin 5**                           | Type-safe GraphQL client generating Kotlin models from Shopify Storefront `.graphql` operations.                   |
| **Shopify Storefront API**                    | Backend commerce source for products, variants, and pricing (GraphQL global IDs).                                  |
| **Hilt + KSP**                                | Compile-time dependency injection wired at the composition root and in `:core:data`.                               |
| **Room**                                      | Local persistence powering offline product browsing and caching.                                                   |
| **DataStore Preferences**                     | Lightweight key-value storage for user/app preferences.                                                            |
| **Firebase**                                  | Authentication, Firestore, App Check, and Firebase AI for the assistant.                                           |
| **Credential Manager + Google Identity**      | Modern Google Sign-In flow.                                                                                        |
| **Paymob SDK**                                | In-app payment processing (with Retrofit/OkHttp for the payment API surface).                                      |
| **Google Maps Compose + Places**              | Map-based address selection in the checkout flow.                                                                  |
| **Ollama provider**                           | Local LLM backend option routed through the AI agent alongside Firebase AI.                                        |
| **Coil**                                      | Async image loading for product imagery.                                                                           |
| **Lottie**                                    | Vector animations (onboarding, empty/loading states).                                                              |
| **Navigation Compose**                        | Type-safe screen navigation driven by `:core:navigation`.                                                          |
| **Timber**                                    | Structured logging.                                                                                                |
| **JUnit · MockK · Turbine · Coroutines Test** | Unit testing stack for ViewModels and repositories.                                                                |

## Module Structure

```plaintext
Dukkan/
├── app/                      # Composition root: DukkanApp (@HiltAndroidApp), MainActivity, AppNavGraph
├── build-logic/              # Composite-build convention plugins (dukkan.feature, dukkan.hilt)
├── core/
│   ├── domain/               # Pure Kotlin: domain models, repository interfaces, use cases
│   ├── data/                 # Repo impls over Apollo (Shopify), Firebase, and Room + mappers + Hilt DI
│   ├── ai_agent/             # LLM abstraction: providers (Firebase AI, Ollama), orchestrator, model router
│   ├── navigation/           # Screen sealed class — single source of truth for all routes
│   └── design_system/        # AppTheme, typography, colors, reusable Compose components
├── feature/                  # Commerce & platform features
│   ├── auth/                 # Firebase auth + Google Sign-In
│   ├── address/              # Google Maps address selection
│   ├── payment/              # Paymob checkout integration
│   ├── chatbot/              # AI shopping assistant UI (drives :core:ai_agent)
│   └── ads/                  # Promotional content
└── features/                 # User-facing shopping screens (MVVM)
    ├── home/                 # Storefront landing
    ├── categories/           # Category browsing
    ├── brands/               # Brand browsing
    ├── search/               # Product search
    ├── product_details/      # Product detail + variant selection
    ├── shopping_cart/        # Cart management
    ├── favorites/            # Wishlist
    ├── order_list/           # Order history
    ├── onboarding/           # First-run onboarding flow
    └── settings/             # User & app settings
```

## Key Features

- **Shopify-backed catalog** — products, variants, and pricing served over the Storefront GraphQL
  API via Apollo.
- **Offline mode** — Room-cached product data keeps browsing available without a connection.
- **AI shopping assistant** — a chatbot backed by a multi-provider agent that routes tasks between
  Firebase AI and a local Ollama model.
- **Full checkout flow** — cart, Google Maps address selection, and Paymob payment processing.
- **Authentication** — Firebase auth with modern Google Sign-In via Credential Manager, hardened
  with Firebase App Check.
- **Complete shopping surface** — home, categories, brands, search, favorites, order history, and
  settings.
- **Strictly modular codebase** — clean-architecture boundaries enforced through Gradle convention
  plugins for fast, isolated builds.

## Team

* [Mohannad El-Sayeh](https://github.com/mSaayeh)
* [Hazem Abdelraouf](https://github.com/Hazem-0)
* [Ahlam Gomaa](https://github.com/Ahlamgomaa)
* [Thaowpsta Saiid Aziz](https://github.com/Thaowpsta)
