# Adding AI Features to Dukkan

This guide explains how to build a new AI-powered feature in the Dukkan project by integrating with the `core:ai_agent` module. It uses the same architecture as the existing AI Search feature.

## Architecture Overview
The `core:ai_agent` module provides an `AiAgentOrchestrator` which connects to our underlying LLM providers (Firebase/Ollama). To create a new feature, you define an `AiTask` that manages the interaction between the LLM, the app's repositories, and the defined tools.

---

## Step-by-Step Integration Guide

### 1. Define Input and Output Models
First, determine what the input to your feature is, and what domain model it should ultimately return.

```kotlin
// The data required to start your task
data class MyFeatureInput(val sessionId: String? = null, val prompt: String)

// Your domain model for the final result
data class MyFeatureResult(val message: String, val isSuccessful: Boolean) 
```

### 2. Create an `AiTask` Implementation
Create a handler class that implements `AiTask<Input, Output>`. This class is where you define system prompts, specify the tools available to the LLM, and handle the multi-turn conversation loop.

```kotlin
import com.dukkan.ai_agent.contract.*
import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyFeatureTask @Inject constructor(
    // Inject any repositories your feature's tools might need to execute
) : AiTask<MyFeatureInput, MyFeatureResult> {
    
    // A unique identifier for analytics or debugging
    override val taskId: String = "MyFeatureTask"

    override suspend fun execute(
        input: MyFeatureInput,
        orchestrator: AiAgentOrchestrator
    ): Result<MyFeatureResult> {
        
        // 1. Setup the initial prompt and tools
        val history = mutableListOf<AiMessage>(
            AiMessage(role = AiRole.User, text = input.prompt)
        )
        
        val tools = listOf(
            ToolDefinition(
                name = "perform_action",
                description = "Does a specific action in the app on behalf of the user.",
                properties = mapOf(
                    "param" to ToolParameter(ToolParameterType.String, "A parameter for the action")
                ),
                required = listOf("param")
            )
        )

        // 2. Loop to allow the LLM to call tools and reason (e.g., max 4 turns)
        var turns = 0
        while(turns < 4) {
            val request = AiRequest(
                systemInstruction = "You are a helpful assistant for Dukkan. Use tools when necessary.",
                messages = history,
                tools = tools
            )
            
            // Ask the Orchestrator to generate a response
            val responseResult = orchestrator.generate(request)
            if (responseResult.isFailure) return Result.failure(Exception("AI Generation Failed"))
            
            val response = responseResult.getOrThrow()
            
            // Append the Model's response to history
            history.add(AiMessage(
                role = AiRole.Model, 
                text = response.text.orEmpty(), 
                toolCalls = response.toolCalls
            ))

            // 3. Handle Completion (No more tool calls means the LLM is done)
            if (response.toolCalls.isEmpty()) {
                return Result.success(MyFeatureResult(response.text ?: "Success", true))
            }
            
            // 4. Handle Tool Calls
            val toolCall = response.toolCalls.first()
            if (toolCall.name == "perform_action") {
                val param = toolCall.arguments["param"]?.toString()?.trim('"')
                
                // Do some domain work here using your injected repositories...
                // val resultData = myRepository.doAction(param)
                
                // Provide the tool result back to the LLM in the next turn
                history.add(AiMessage(
                    role = AiRole.Tool, 
                    toolResultName = toolCall.name, 
                    toolResultId = toolCall.id, 
                    // toolResult is typically a JsonElement containing the result
                    toolResult = kotlinx.serialization.json.JsonPrimitive("Action completed successfully.") 
                ))
            }
            turns++
        }
        
        return Result.failure(Exception("Max conversational steps reached"))
    }
}
```

### 3. Register the Task in Dependency Injection (Hilt)
Your task needs to be bound into a Dagger Multi-binding set (`Set<AiTask<*, *>>`) so the `TaskRegistry` can dynamically provide it to the Orchestrator. You typically do this in your feature's Hilt module:

```kotlin
import com.dukkan.ai_agent.contract.AiTask
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class MyFeatureModule {

    @Binds
    @IntoSet // VERY IMPORTANT: Binds it into the set of tasks
    abstract fun bindMyFeatureTask(
        impl: MyFeatureTask
    ): AiTask<*, *>
}
```

### 4. Execute the Task via the Orchestrator
Finally, to execute your AI feature from a Repository or ViewModel, inject the `AiAgentOrchestrator` and use its `run` method, passing in your Task class and Input object.

```kotlin
import com.dukkan.ai_agent.orchestrator.AiAgentOrchestrator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class MyFeatureRepositoryImpl @Inject constructor(
    private val orchestrator: AiAgentOrchestrator
) : MyFeatureRepository {
    
    override fun startFeature(prompt: String): Flow<MyFeatureResult> = flow {
        // Run the task through the orchestrator
        val result = orchestrator.run(
            MyFeatureTask::class.java, 
            MyFeatureInput(prompt = prompt)
        )
        
        // Emit the final Result (or an Error if it failed)
        emit(result.getOrElse { MyFeatureResult("Error", false) })
    }
}
```

## Summary
By encapsulating your logic inside an `AiTask`, you keep your prompt engineering, tool definitions, and multi-turn loops isolated and testable. The `AiAgentOrchestrator` securely abstracts away the underlying provider network requests (Firebase, Ollama) and cleanly bridges your domain with AI capabilities.
