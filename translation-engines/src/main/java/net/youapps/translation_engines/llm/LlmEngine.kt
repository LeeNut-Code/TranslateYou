/*
 * Copyright (c) 2026 You Apps
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.youapps.translation_engines.llm

import net.youapps.translation_engines.ApiKeyState
import net.youapps.translation_engines.EngineSettingsProvider
import net.youapps.translation_engines.Language
import net.youapps.translation_engines.RetrofitHelper
import net.youapps.translation_engines.Translation
import net.youapps.translation_engines.TranslationEngine
import net.youapps.translation_engines.llm.obj.ChatCompletionRequest
import net.youapps.translation_engines.llm.obj.LlmLanguages
import net.youapps.translation_engines.llm.obj.TranslationContentParser
import net.youapps.translation_engines.llm.obj.TranslationPrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Generic OpenAI Chat Completions compatible translation engine.
 *
 * Not bound to any single provider (DeepSeek, OpenAI, Ollama, OpenRouter, ...).
 * Users configure Base URL, optional API Key and a free-form Model ID.
 */
class LlmEngine(settingsProvider: EngineSettingsProvider) : TranslationEngine(settingsProvider) {
    override val name = "LLM (OpenAI-compatible)"
    override val defaultUrl = "https://api.openai.com/v1/"
    override val urlModifiable = true
    override val apiKeyState = ApiKeyState.OPTIONAL
    override val supportedModels: List<String> = emptyList()
    override val supportsCustomModelId: Boolean = true
    override val supportsLanguageDetection: Boolean = true

    private lateinit var api: LlmApi

    override fun createOrRecreate(): TranslationEngine = apply {
        // LLM requests carry user text and the Authorization key, so logging must be disabled.
        api = RetrofitHelper.createInstance(baseUrl = getUrl(), logLevel = HttpLoggingInterceptor.Level.NONE)
    }

    override suspend fun getLanguages(): List<Language> {
        return LlmLanguages.supportedLanguages
    }

    override suspend fun translate(query: String, source: String?, target: String): Translation {
        val model = getSelectedModel()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("Model ID is required for LLM engine")

        val targetLang = LlmLanguages.supportedLanguages.find { it.code == target }
            ?: Language(code = target, name = target)
        val sourceLang = source?.let { sourceCode ->
            LlmLanguages.supportedLanguages.find { language -> language.code == sourceCode }
                ?: Language(code = sourceCode, name = sourceCode)
        }

        val system = TranslationPrompt.build(target = targetLang, source = sourceLang)
        val messages = listOf(system, TranslationPrompt.userMessage(query))

        val request = ChatCompletionRequest(model = model, messages = messages, stream = false)

        val authorization = getApiKey()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { "Bearer $it" }

        val response = withContext(Dispatchers.IO) {
            api.createChatCompletion(authorization = authorization, request = request)
        }

        val content = response.choices
            ?.firstOrNull()
            ?.message
            ?.content
            ?.trim()

        if (content.isNullOrEmpty()) {
            throw IllegalStateException("LLM returned no translated text")
        }

        val parsed = TranslationContentParser.parse(content)
        if (parsed.translatedText.isBlank()) {
            throw IllegalStateException("LLM returned no translated text")
        }

        return Translation(
            translatedText = parsed.translatedText,
            detectedLanguage = parsed.detectedLanguage
        )
    }
}
