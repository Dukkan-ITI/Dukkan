# Ollama Cloud Free Integration Plan

## Goal

Replace the current Gemini/Firebase AI search provider with Ollama Cloud Free while keeping the same user-facing AI search feature:

- Shopper writes natural language query.
- AI decides whether query is clear enough.
- AI may ask one clarification.
- AI calls Shopify search with structured filters.
- App displays product cards from Shopify.
- AI only writes short helpful summary.

## Short Answer

This is not a totally different feature.

The current code already has the right architecture: a repository owns the AI loop, the UI consumes `AgenticSearchResult`, and Shopify search remains the source of truth.

Main difference:

- Gemini SDK stores chat and tool calls for us.
- Ollama Cloud is HTTP API, so app must manage chat messages and tool responses manually.
- Ollama Cloud API key must not live inside Android app. A backend proxy is strongly recommended.

## Current Gemini Flow

Current important files:

- `core/domain/src/main/java/com/dukkan/domain/repository/GeminiRepository.kt`
- `core/domain/src/main/java/com/dukkan/domain/usecase/search/AgenticSearchUseCase.kt`
- `core/domain/src/main/java/com/dukkan/domain/usecase/search/ResumeSearchClarificationUseCase.kt`
- `core/domain/src/main/java/com/dukkan/domain/model/AgenticSearchResult.kt`
- `core/data/src/main/java/com/dukkan/data/repository/GeminiRepositoryImpl.kt`
- `core/data/src/main/java/com/dukkan/data/source/GeminiRemoteSource.kt`
- `core/data/src/main/java/com/dukkan/data/util/GeminiConstants.kt`
- `core/data/src/main/java/com/dukkan/data/mapper/GeminiFunctionCallMapper.kt`
- `core/data/src/main/java/com/dukkan/data/di/GeminiModule.kt`
- `features/search/src/main/java/com/dukkan/search/viewmodel/SearchViewModel.kt`

Current flow:

1. User taps AI search.
2. `SearchViewModel.onAiSearchTriggered()` calls `AgenticSearchUseCase`.
3. `AgenticSearchUseCase` calls `GeminiRepository.startSearch()`.
4. `GeminiRepositoryImpl` starts Firebase Gemini chat.
5. Gemini returns either:
   - plain text,
   - `ask_clarifying_question`,
   - `search_shopify_products`.
6. If clarification: app stores session and waits for user answer.
7. If Shopify tool: app searches Shopify, sends function response back to Gemini, then emits products and AI message.
8. UI renders product cards and message.

## Target Ollama Cloud Free Flow

Target flow stays same from UI side:

1. User taps AI search.
2. `SearchViewModel` calls same use case.
3. Repository sends chat request to backend.
4. Backend calls Ollama Cloud `/api/chat`.
5. Ollama returns either:
   - message content,
   - `tool_calls`.
6. App/backend handles selected tool:
   - `ask_clarifying_question`
   - `search_shopify_products`
7. Shopify result goes back to Ollama as tool message.
8. Final AI message and Shopify products go back to app.

## Recommended Architecture

Use backend proxy.

Android app should not call `https://ollama.com/api` directly because API key would be extractable from APK.

Recommended split:

- Android app calls your backend endpoint, for example `/ai/search`.
- Backend stores Ollama API key.
- Backend calls Ollama Cloud.
- Backend applies rate limits and abuse protection.
- Android keeps same UI flow.

## Why Backend Proxy Matters

Ollama Cloud needs API key for direct programmatic access.

Putting API key in Android app means anyone can decompile APK and use your free quota.

Backend gives:

- secret protection,
- request limits,
- better logging,
- model switching without app release,
- fallback handling,
- easier cost/quota control.

## Code Change Cost

Meaning: how different code becomes, not money.

Overall code difference: medium.

Not a rewrite. More like provider swap plus naming cleanup.

### Low Change Areas

These can mostly stay same:

- Search screen UI.
- Search loading animation.
- Clarification UI.
- Product card rendering.
- Shopify `SearchRepository`.
- `AgenticSearchResult` shape.
- Cache idea.
- Retry idea.
- Max turn limits.

### Medium Change Areas

These need refactor:

- Rename Gemini-specific domain names.
- Replace Firebase AI SDK source.
- Add Ollama request/response models.
- Parse Ollama `tool_calls`.
- Store chat history manually.
- Send tool results as `role = tool`.

### High Risk Areas

These need careful testing:

- Tool call argument parsing.
- Clarification session resume.
- Error mapping.
- Cloud quota/rate-limit behavior.
- Network timeout behavior.
- Model following tool schema reliably on Free plan.

## Exact Refactor Plan

### Phase 1: Rename Gemini Concepts

Purpose: make app provider-neutral before Ollama swap.

Rename:

- `GeminiRepository` to `AiSearchRepository`
- `GeminiRepositoryImpl` to `AiSearchRepositoryImpl` or `OllamaAiSearchRepositoryImpl`
- `GeminiRemoteSource` to `OllamaRemoteSource`
- `GeminiConstants` to `AiSearchConstants` or `OllamaConstants`
- `GeminiError` to `AiSearchError`
- `GeminiFunctionCallMapper` to `AiToolCallMapper`
- `GeminiModule` to `AiSearchModule`

Expected impact:

- Mostly imports and class names.
- UI logic barely changes.
- Good first commit.

### Phase 2: Remove Firebase AI From AI Search

Remove usage of:

- `com.google.firebase.ai.Chat`
- `com.google.firebase.ai.GenerativeModel`
- `FunctionDeclaration`
- `FunctionResponsePart`
- `GenerateContentResponse`
- Firebase AI-specific tool schema classes.

Keep Firebase Auth/Firestore if app still uses them.

`firebase-ai` Gradle dependency can be removed only if no other feature uses Firebase AI.

### Phase 3: Add Ollama API Contract

Need data models for Ollama chat:

- request model,
- message model,
- tool definition model,
- tool call model,
- response model,
- error model.

No official Kotlin SDK is used here. Use HTTP client already suitable for Android.

Current project already uses Retrofit/OkHttp in payment feature, but `core:data` only has Apollo/Firebase now. Options:

- Add Retrofit/OkHttp to `core:data`.
- Or use OkHttp directly.

Recommended: Retrofit + kotlinx serialization if team prefers typed API. OkHttp direct is smaller but more manual.

### Phase 4: Decide Where AI Loop Lives

Two valid choices.

#### Option A: AI Loop In Android

Android stores messages and calls backend only as Ollama proxy.

Pros:

- Smaller backend.
- Current repository logic stays close.

Cons:

- More app complexity.
- Backend still must expose proxy.
- Tool flow spread across app and backend.

#### Option B: AI Loop In Backend

Android sends query/session answer. Backend handles Ollama conversation and Shopify tool choice.

Pros:

- Best security.
- Cleaner Android app.
- Easier model and prompt changes.
- Better quota control.

Cons:

- More backend work.
- Need backend session store.

Recommendation: Option B for production. Option A acceptable for graduation/demo speed.

## Android-Only Minimal Plan

Use this only if backend is not available yet.

1. Keep current repository loop in Android.
2. Replace Gemini SDK calls with backend proxy calls.
3. Backend endpoint only forwards request to Ollama Cloud.
4. Android keeps sessions in `ConcurrentHashMap`.
5. Android sends full chat history each turn.

This is fastest, but still needs backend to protect API key.

## Production Backend Plan

Best final architecture.

Backend endpoint examples:

- `POST /ai-search/start`
- `POST /ai-search/resume`

Backend responsibilities:

- store Ollama API key,
- call Ollama Cloud,
- keep conversation state by `sessionId`,
- execute Shopify search tool,
- return clean app response,
- apply rate limits per user/device,
- hide provider details from Android.

Android receives provider-neutral result:

- `awaiting_clarification`
- `success`
- `error`

This makes future provider switch cheap: Ollama, OpenAI, Gemini, local model, etc.

## Tool Definitions

Keep same tools.

### `ask_clarifying_question`

Use when query is too broad.

Input:

- `question`: short user-facing question.

Output to app:

- `AgenticSearchResult.AwaitingClarification`

### `search_shopify_products`

Use when enough shopping intent exists.

Input:

- `query`
- `category`
- `color`
- `size`
- `maxPrice`
- `availableOnly`

Output:

- Backend/app calls Shopify.
- Shopify products become tool result.
- Ollama writes final summary.

## Prompt Plan

Keep current prompt mostly same.

Add Ollama-specific constraints:

- Always use tool call for product search.
- Never invent products, prices, discounts, brands, inventory, or policies.
- Ask clarification only if needed.
- Do not list all products because UI renders cards.
- Final answer should be short.
- If Shopify returns zero products, suggest broader search.

## Model Plan For Free Tier

Start with smaller/free cloud model that supports tool calling.

Selection criteria:

- supports tool calling,
- good instruction following,
- fast enough,
- low usage level,
- stable with JSON arguments.

Avoid using very large cloud model on Free unless needed, because Free has light usage and one concurrent cloud model.

## Error Mapping Plan

Keep current UI messages, rename internally.

Map Ollama/backend errors to:

- `RateLimited`: HTTP 429, quota, usage limit.
- `Timeout`: network timeout, HTTP 503/504.
- `Unknown`: unclassified response.
- `ClarificationLimitReached`: same local/business rule.
- `MaxStepsReached`: too many tool calls.
- `SearchFailed`: Shopify failure.
- `TimeoutExceeded`: AI loop passed max turns.

Add one likely new error:

- `ConfigurationMissing`: backend missing Ollama key or model not configured.

## Session Plan

Current sessions live in Android map. Works but dies when app process dies.

For production backend:

- session stored on backend,
- session expires after 10-20 minutes,
- max 3 clarification turns,
- max 2 Shopify calls,
- max 4 AI turns,
- cleanup after success/error.

For Android-only proxy:

- keep current `ConcurrentHashMap`,
- replace `Chat` object with list of Ollama messages.

## Cache Plan

Keep current 10-minute cache.

Cache by normalized query:

- lowercase,
- trim spaces,
- include filters if used.

Cache only successful final results.

Do not cache clarification state.

## Gradle Dependency Plan

Likely changes:

- Remove `firebase-ai` if unused.
- Keep Firebase Auth and Firestore.
- Add HTTP dependency to `core:data` if not already usable there.

Potential dependency:

- Retrofit + OkHttp.

No change needed in UI modules for dependency.

## File-by-File Impact

### `core/domain`

Expected changes:

- Rename repository interface.
- Rename error enum.
- Update use cases to depend on provider-neutral repository.
- Keep `AgenticSearchResult` mostly same.

Code cost: low.

### `core/data`

Expected changes:

- Replace Gemini remote source with Ollama/backend remote source.
- Replace Gemini response parsing with Ollama response parsing.
- Replace Gemini function response format with Ollama tool message format.
- Update DI binding.
- Update Gradle dependencies.

Code cost: medium/high.

### `features/search`

Expected changes:

- Replace `GeminiError` import with `AiSearchError`.
- UI behavior stays same.
- String resources can stay same if user-facing brand remains "Dukkan AI".

Code cost: low.

### `AGENTIC_SEARCH.md`

Expected changes:

- Rewrite docs from Gemini to Ollama/provider-neutral AI.

Code cost: low.

## Testing Plan

### Unit Tests

Add tests for:

- Ollama tool call parsed into `ShopifySearchToolArgs`.
- Clarification tool parsed into `ClarificationRequest`.
- Unknown tool returns safe error.
- Empty AI message falls back to standard Shopify search.
- 429 maps to rate-limited message.
- 503/timeout maps to timeout message.
- Shopify failure maps to `SearchFailed`.
- Cache returns previous success.

### Integration Tests

Test with fake backend/Ollama:

- clear query: "red shoes under 1000"
- broad query: "shoes"
- clarification answer: "running shoes size 42"
- no products found
- model returns malformed arguments
- model calls too many tools

### Manual QA

Check:

- AI loading border starts/stops correctly.
- Cancel AI search works.
- Retry works.
- Arabic/English strings still fit.
- No product duplication in AI message.
- Products shown come only from Shopify.

## Migration Steps

1. Create provider-neutral names in domain.
2. Keep Gemini implementation temporarily.
3. Add Ollama/backend remote source.
4. Switch DI from Gemini implementation to Ollama implementation.
5. Remove Firebase AI imports from AI search.
6. Remove `firebase-ai` dependency if unused.
7. Update docs.
8. Run unit tests.
9. Run Android app and test AI search manually.
10. Monitor Free usage and rate-limit behavior.

## Rollback Plan

Keep one branch/tag before switching DI.

Rollback is easy if:

- domain interface stays same,
- UI stays same,
- Gemini implementation is not deleted until Ollama works.

Recommended migration style:

1. Add provider-neutral interface.
2. Keep Gemini as old provider.
3. Add Ollama as new provider.
4. Switch binding.
5. Delete Gemini after verification.

## Estimated Implementation Time

Android-only with backend proxy:

- Rename/domain cleanup: 2-4 hours.
- Ollama request/response models: 3-5 hours.
- Repository loop conversion: 5-8 hours.
- Error handling/cache/session cleanup: 2-4 hours.
- Tests/manual QA: 4-8 hours.

Total: 16-29 hours.

Production backend AI loop:

- Backend endpoint/session/rate limit: 8-16 hours.
- Android API integration: 4-8 hours.
- Domain rename/cleanup: 2-4 hours.
- Tests/manual QA: 6-10 hours.

Total: 20-38 hours.

## Money Cost With Ollama Cloud Free

Ollama Cloud Free:

- Monthly subscription: $0.
- Best for light usage.
- One cloud model at a time.
- Usage limits reset by Ollama plan rules.

Expected direct AI provider bill for demo/small testing: $0.

Possible non-AI costs:

- backend hosting,
- logs/monitoring,
- network traffic,
- developer time.

## Main Risks

1. Free tier may hit usage limits during demos.
2. Tool calling quality depends on selected model.
3. API key must be protected.
4. Android local sessions can expire on process death.
5. Direct mobile-to-Ollama Cloud is insecure.

## Recommended Final Decision

Use Ollama Cloud Free through backend proxy.

Keep Android UI and use cases mostly same.

Rename Gemini-specific names to provider-neutral AI names.

Move provider-specific logic into one small data layer source.

This gives lowest long-term code cost and keeps future provider changes easy.

## Ollama Best Practices From Docs

Sources checked:

- [Ollama Cloud](https://docs.ollama.com/cloud)
- [Ollama Chat API](https://docs.ollama.com/api/chat)
- [Ollama Tool Calling](https://docs.ollama.com/capabilities/tool-calling)
- [Ollama Structured Outputs](https://docs.ollama.com/capabilities/structured-outputs)
- [Ollama Streaming](https://docs.ollama.com/api/streaming)
- [Ollama Errors](https://docs.ollama.com/api/errors)
- [Ollama Tools Models](https://ollama.com/search?c=tools)
- [Qwen3 model page](https://ollama.com/library/qwen3)
- [Llama 3.1 model page](https://ollama.com/library/llama3.1)

### Best Practice 1: Use Backend Proxy

Ollama Cloud direct API uses `https://ollama.com/api` and requires API key auth.

Do not put that key in Android.

Use backend proxy:

- Android sends user query to backend.
- Backend owns Ollama key.
- Backend calls Ollama Cloud.
- Backend rate-limits per user/device/IP.
- Backend can switch models without app release.

### Best Practice 2: Use `/api/chat`, Not `/api/generate`

Your feature is conversational and tool-based.

Use Ollama `/api/chat` because it supports:

- message history,
- system messages,
- tool definitions,
- tool calls,
- tool results.

### Best Practice 3: Disable Streaming First

Set `stream: false` for first implementation.

Reason:

- simpler parsing,
- easier tool-call handling,
- easier error mapping,
- enough for short AI search summaries.

Streaming can come later only for final assistant text, not tool-call phase.

### Best Practice 4: Use Tool Calling, Not Structured Outputs

Important: Ollama docs say Cloud currently does not support structured outputs.

So do not rely on `format` JSON schema for Ollama Cloud Free.

Use `tools` instead:

- `ask_clarifying_question`
- `search_shopify_products`

Then validate tool arguments yourself in backend/app.

### Best Practice 5: Keep Tool Schema Small

For Dukkan search, tool arguments should stay small:

- `query`
- `category`
- `color`
- `size`
- `maxPrice`
- `availableOnly`

Avoid complex nested filters. Shopify search already handles product retrieval.

### Best Practice 6: Validate Every Tool Call

Never trust model arguments directly.

Validate:

- tool name is allowed,
- required fields exist,
- strings are not huge,
- price is sane,
- boolean is boolean,
- unknown fields ignored,
- malformed arguments return safe fallback.

### Best Practice 7: Use Strict Turn Limits

Keep current limits:

- max AI turns: 4,
- max Shopify calls: 2,
- max clarification questions: 3,
- session expiry: 10-20 minutes.

This protects Free plan usage and prevents loops.

### Best Practice 8: Low Temperature

Use low temperature for shopping search.

Recommended:

- `temperature: 0`
- or closest supported low value.

Reason:

- less creative,
- better tool consistency,
- less product hallucination.

### Best Practice 9: Use Product Data As Source Of Truth

Model must never invent:

- products,
- prices,
- brands,
- discounts,
- stock,
- policies.

Only Shopify tool result is truth.

Prompt should say this clearly.

### Best Practice 10: Handle Ollama Error Codes

Map these:

- `400`: bad request or malformed JSON.
- `404`: model missing/unavailable.
- `429`: rate limit or Free tier limit.
- `500`: Ollama server issue.
- `502`: cloud model cannot be reached.

Existing UI error model can handle this with small rename.

## Best Model Choice For Dukkan AI Search

### Primary Recommendation: `qwen3`

Use `qwen3` first.

Why:

- Ollama tool-calling docs use `qwen3` in examples.
- Qwen3 page marks it as `tools` and `thinking`.
- Qwen3 has strong agent/tool capability notes.
- Qwen3 supports multilingual behavior, useful for Arabic/English shoppers.
- Good balance for Free plan testing.

Best starting tag:

- `qwen3`

If cloud requires explicit cloud model name, use available cloud Qwen variant from Ollama model list/account.

### Stronger Fallback: `qwen3.5`

Use if Free plan offers it and `qwen3` quality is weak.

Why:

- Ollama tools model list marks `qwen3.5` as `tools`, `thinking`, and `cloud`.
- Newer than Qwen3.
- Better likely for reliable tool choice.

Risk:

- May consume Free plan faster depending model size/availability.

### Stable Alternative: `llama3.1`

Use if Qwen model unavailable.

Why:

- Ollama marks Llama 3.1 as tools-capable.
- Good general instruction following.
- Large context.

Risk:

- Older than current Qwen/Gemma cloud models.
- May be weaker for JSON/tool argument discipline than Qwen for this exact use.

### Avoid For First Version

Avoid coding-specialized models first:

- `kimi-k2.7-code`
- `north-mini-code`
- `glm-5.2`
- `minimax-m3`

Reason:

- Dukkan feature is shopping intent parsing, not coding.
- They may be strong but unnecessary for Free plan.

Avoid vision/audio models unless product-image search is added later.

## Model Selection Matrix

| Model | Fit | Why | Risk |
|---|---:|---|---|
| `qwen3` | Best first choice | Tool docs use it, tools/thinking, multilingual, agent-friendly | Need verify cloud availability in account |
| `qwen3.5` | Best upgrade | Tools + cloud + newer | Free tier may be tighter |
| `llama3.1` | Good fallback | Tools support, stable general model | Older, may be less strict with args |
| `gemma4` | Possible fallback | Tools + cloud, general model | Need test shopping/tool quality |

## Recommended Free Plan Setup

Start:

- Model: `qwen3`
- Endpoint: backend proxy to `https://ollama.com/api/chat`
- Streaming: off
- Temperature: `0`
- Tools: 2 only
- Thinking: off at first, unless model quality needs it
- Max turns: 4
- Max Shopify calls: 2
- Cache TTL: 10 minutes

Upgrade path:

1. Try `qwen3`.
2. If tool calls bad, try `qwen3.5`.
3. If unavailable, try `llama3.1`.
4. If latency too high, reduce final AI message length and cache more.

## Acceptance Criteria For Chosen Model

Model is good enough only if it passes these manual tests:

1. Query: "red shoes under 1000"
   - Must call `search_shopify_products`.
   - Args include query/color/maxPrice.

2. Query: "I want shoes"
   - Should ask clarification, not search too broad.

3. Query: "black nike hoodie size L"
   - Must search with brand/query, color, size/category if possible.

4. Query: "show me cheapest bags available"
   - Must set `availableOnly` if prompt asks for available products.

5. Shopify returns products.
   - Final message must not list all product names/prices.

6. Shopify returns empty.
   - Final message should suggest broader query.

7. Arabic query.
   - Should create usable English/Shopify search query if Shopify data is English.

8. Malformed/unknown tool call.
   - Backend rejects safely.

## Final Recommendation

Use `qwen3` first.

Keep code provider-neutral.

Use backend proxy.

Do not use structured outputs on Ollama Cloud Free.

Use tool calling with validation.

Move to `qwen3.5` only if `qwen3` fails tool accuracy tests.
