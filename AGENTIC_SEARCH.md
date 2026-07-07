# Agentic Search

The Agentic Search feature lets shoppers find products with natural language. It now uses an Ollama-compatible chat flow. Shopify remains the source of truth for product data.

## Core Pieces

### 1. `AiSearchRepositoryImpl`

Owns the conversational search loop:

- starts AI search sessions,
- keeps message history for clarification turns,
- calls Ollama through `OllamaRemoteSource`,
- handles tool calls,
- calls Shopify through `SearchRepository`,
- emits typed `AgenticSearchResult` values to the UI,
- retries transient failures,
- caches successful results for 10 minutes.

### 2. `OllamaRemoteSource`

Builds Ollama `/api/chat` requests:

- model defaults to `qwen3`,
- base URL defaults to `https://ollama.com/api`,
- streaming is disabled for simpler tool-call handling,
- temperature is `0`,
- tools are declared as JSON function schemas.

Runtime config comes from Gradle properties:

- `ollamaBaseUrl`
- `ollamaModel`
- `ollamaApiKey`

For production, use a backend proxy and keep the Ollama API key off-device.

### 3. Available AI Tools

`ask_clarifying_question`

- Used when the shopper query is too broad.
- Returns one short question.
- UI pauses until shopper answers.

`search_shopify_products`

- Used when enough intent exists.
- Arguments include query, category, color, size, max price, and availability.
- App executes Shopify search and sends compact product data back to Ollama.

## Flow

1. User triggers AI search in `SearchViewModel`.
2. `AgenticSearchUseCase` calls `AiSearchRepository.startSearch(query)`.
3. Repository sends chat request to Ollama.
4. Ollama may ask clarification or call Shopify search tool.
5. Repository maps tool arguments into `SearchFilter`.
6. Shopify search returns product cards.
7. Repository sends tool result to Ollama.
8. Ollama writes short summary.
9. UI shows summary plus Shopify product cards.

## Error Handling

Errors are mapped to `AiSearchError`:

- `ConfigurationMissing`
- `RateLimited`
- `Timeout`
- `ClarificationLimitReached`
- `MaxStepsReached`
- `SearchFailed`
- `TimeoutExceeded`
- `Unknown`

The UI maps these to localized strings.

## Limits

- Max AI turns: 4
- Max Shopify tool calls: 2
- Max clarification questions: 3
- Cache size: 100 entries
- Cache TTL: 10 minutes

## Security Note

Direct Android-to-Ollama Cloud works for development only if the API key is injected locally. It is not safe for production because APK secrets can be extracted. Production should route Android requests through a backend proxy.
