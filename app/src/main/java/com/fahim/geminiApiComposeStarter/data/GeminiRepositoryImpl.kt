package com.fahim.geminiApiComposeStarter.data

import android.util.Log
import com.fahim.geminiApiComposeStarter.data.local.ChatMessageDao
import com.fahim.geminiApiComposeStarter.data.local.ChatMessageEntity
import com.fahim.geminiApiComposeStarter.data.security.SecurityUtils
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val TAG = "GeminiRepository"
private const val DEFAULT_MODEL = "gemini-3.6-flash"

class GeminiRepositoryImpl(
    apiKey: String,
    private val chatMessageDao: ChatMessageDao? = null,
    modelName: String = DEFAULT_MODEL,
) : GeminiRepository {

    private val model = GenerativeModel(modelName = modelName, apiKey = apiKey)

    override fun getChatHistory(): Flow<List<ChatMessageEntity>> {
        return chatMessageDao?.getAllMessages() ?: emptyFlow()
    }

    override suspend fun saveMessage(sender: String, content: String) {
        val encryptedContent = SecurityUtils.encrypt(content)
        chatMessageDao?.insertMessage(
            ChatMessageEntity(sender = sender, content = encryptedContent)
        )
    }

    override suspend fun clearHistory() {
        chatMessageDao?.clearAllMessages()
    }

    override suspend fun generateText(prompt: String): Result<String> = try {
        val response = model.generateContent(prompt)
        val text = response.text?.takeIf { it.isNotBlank() }
        if (text != null) {
            Result.success(text)
        } else {
            Result.failure(IllegalStateException("Empty response from Gemini"))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "generateContent failed", e)
        Result.failure(e)
    }
}
