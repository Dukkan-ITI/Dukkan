# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Project Overview

Dukkan is a multi-module Android e-commerce app built with Jetpack Compose. It is backed by the **Shopify Storefront GraphQL API**. The app shell wires together independent `core` and `feature` modules.

## Architecture

The project follows a Clean Architecture approach across three main module groups, with dependencies pointing inward toward `:core:domain`:
- `:core:domain`: Pure Kotlin/JVM module for domain models, repository interfaces, and use cases.
- `:core:data`: Repository implementations (Apollo/Shopify, Firebase, Room). Hilt bindings are in `di/DataModule.kt`.
- `:core:navigation`: Route definitions.
- `:core:design_system`: Shared Compose UI, Theme, and reusable components.
- `:feature:*`: Self-contained screens following MVVM architecture.
- `:app`: Composition root containing the single `NavHost` and wiring everything together.

## Important Rules for AI Agents

- **String Resources**: **ALL user-facing strings MUST be added to the Android string resources (`res/values/strings.xml`)**. Do not hardcode strings in UI components, view models, or other code files.
- **Build Conventions**: Features should typically use the `dukkan.feature` Gradle plugin defined in `build-logic/convention/`.
- **GraphQL**: The app uses the Apollo GraphQL plugin in `core/data`. Modifying `.graphql` files requires regeneration via Gradle build.
- **Package Names**: Package names are sometimes inconsistent across modules. Always match the existing package of the module you are editing.
- **Routing**: Use `Screen` sealed class in `:core:navigation` as the single source of truth for routes.
