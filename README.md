# Dukkan

> **A production-grade Android e-commerce application built with Jetpack Compose, Clean Architecture, and Shopify's Storefront GraphQL API.**

Dukkan is a modern, scalable shopping application designed to demonstrate production-level Android engineering practices. The project combines a modular Clean Architecture with real-world commerce features including secure authentication, offline support, AI-powered shopping assistance, online payments, and location-based checkout.

The application emphasizes **maintainability**, **scalability**, **testability**, and **separation of concerns**, making it suitable as both a learning resource and a reference architecture for large Android applications.

---

## ✨ Features

- 🛍️ Shopify Storefront GraphQL integration
- 🤖 AI shopping assistant with multiple LLM providers
- 🔐 Firebase Authentication with Google Sign-In
- 🛒 Complete shopping cart and checkout flow
- 💳 Paymob payment integration
- 📍 Google Maps address selection
- ❤️ Wishlist and favorites
- 📦 Order history
- 🔍 Product search
- 📱 Offline browsing using Room cache
- 🌐 Deep Links & App Links
- 🎨 Fully built with Jetpack Compose (Material 3)

---

# 🏗️ Architecture

Dukkan follows **Clean Architecture** with **MVVM** and a fully modular Gradle structure. Every dependency points inward toward a pure Kotlin domain layer, allowing features to evolve independently while remaining highly testable.

```text
Presentation (Compose + MVVM)
            │
        Use Cases
            │
 Repository Interfaces
            │
──────────────────────────────
 Data Layer
 ├── Shopify (Apollo GraphQL)
 ├── Firebase
 └── Room Cache
```

## Architecture Highlights

| Layer | Responsibility |
|--------|---------------|
| **app** | Composition root, navigation graph, dependency initialization |
| **core:domain** | Business models, repository contracts, use cases |
| **core:data** | Repository implementations, remote/local data sources, mappers |
| **feature** | Independent application features following MVVM |
| **build-logic** | Convention plugins for shared Gradle configuration |
| **core:ai_agent** | Provider-agnostic AI orchestration layer |

## Engineering Principles

- Clean Architecture
- MVVM
- SOLID Principles
- Repository Pattern
- Dependency Injection
- Unidirectional Data Flow (UDF)
- Immutable UI State
- Single Source of Truth (SSOT)
- Modular Design

---

# 🛠️ Tech Stack

| Category | Technologies |
|-----------|--------------|
| **Language** | Kotlin 2.0 |
| **UI** | Jetpack Compose, Material 3 |
| **Architecture** | Clean Architecture, MVVM |
| **Dependency Injection** | Hilt, KSP |
| **Networking** | Apollo Kotlin 5, Retrofit, OkHttp |
| **Backend** | Shopify Storefront GraphQL API |
| **Authentication** | Firebase Authentication, Credential Manager, Google Identity |
| **Database** | Room |
| **Preferences** | DataStore Preferences |
| **Maps** | Google Maps Compose, Places API |
| **Payments** | Paymob SDK |
| **AI** | Firebase AI, Ollama |
| **Image Loading** | Coil |
| **Animations** | Lottie |
| **Navigation** | Navigation Compose |
| **Logging** | Timber |
| **Testing** | JUnit, MockK, Turbine, Coroutines Test |

---

# 📦 Project Structure

```text
Dukkan/
├── app/                      # Composition root
├── build-logic/              # Convention plugins
├── core/
│   ├── ai_agent/             # AI abstraction & providers
│   ├── data/                 # Repository implementations
│   ├── design_system/        # Shared Compose UI
│   ├── domain/               # Business logic & use cases
│   └── navigation/           # Shared navigation
├── feature/
│   ├── address/
│   ├── ads/
│   ├── auth/
│   ├── chatbot/
│   └── payment/
└── features/
    ├── home/
    ├── categories/
    ├── brands/
    ├── search/
    ├── product_details/
    ├── shopping_cart/
    ├── favorites/
    ├── order_list/
    ├── onboarding/
    └── settings/
```

---

# 🧩 Core Components

## 🏛️ Domain Layer

- Pure Kotlin module
- Business models
- Repository interfaces
- Use cases
- Framework independent

---

## 📦 Data Layer

Repository implementations are backed by three independent data sources:

- Shopify Storefront GraphQL API (Apollo Kotlin)
- Firebase Authentication & Firestore
- Room local database

Dedicated mapper classes convert remote and local models into domain models.

---

## 🎨 Design System

A shared UI foundation containing:

- Material 3 Theme
- Typography
- Color System
- Reusable Compose Components

---

## 🤖 AI Agent

The AI module abstracts multiple LLM providers behind a common interface.

Supported providers include:

- Firebase AI
- Ollama

An orchestration layer dynamically routes requests to the most suitable provider depending on the task.

---

# 🚀 Highlights

- Production-grade modular architecture
- Type-safe GraphQL with Apollo Kotlin
- Offline-first data caching
- AI-powered shopping assistant
- Secure authentication with Firebase
- Google Maps checkout experience
- Paymob payment integration
- Deep Links & Android App Links
- Convention plugins for scalable Gradle configuration
- Comprehensive unit testing support

---

# 👥 Team

| Member | GitHub |
|--------|--------|
| Mohannad El-Sayeh | https://github.com/mSaayeh |
| Hazem Abdelraouf | https://github.com/Hazem-0 |
| Ahlam Gomaa | https://github.com/Ahlamgomaa |
| Thaowpsta Saiid Aziz | https://github.com/Thaowpsta |

---

## 📄 License

This project was developed as part of the **Information Technology Institute (ITI)** Mobile Application Development Program and is intended for educational and portfolio purposes.
