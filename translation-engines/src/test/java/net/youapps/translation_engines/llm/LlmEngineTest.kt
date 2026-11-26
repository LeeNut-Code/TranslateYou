package net.youapps.translation_engines.llm

import net.youapps.translation_engines.JsonHelper
import net.youapps.translation_engines.llm.obj.ChatCompletionRequest
import net.youapps.translation_engines.llm.obj.ChatMessage
import net.youapps.translation_engines.llm.obj.LlmLanguages
import net.youapps.translation_engines.llm.obj.TranslationContentParser
import net.youapps.translation_engines.llm.obj.TranslationPrompt
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmEngineTest {

    @Test
    fun prompt_contains_target_and_source() {
        val target = LlmLanguages.supportedLanguages.find { it.code == "zh-rCN" }!!
        val system = TranslationPrompt.build(target = target, source = null)

        assertEquals("system", system.role)
        assertTrue(system.content.contains("zh-rCN"))
        assertTrue(system.content.contains("auto-detect"))
        assertTrue(system.content.contains("translation"))
        assertTrue(system.content.contains("detectedLanguage"))
        assertTrue(system.content.contains("Preserve paragraphs, line breaks"))
    }

    @Test
    fun prompt_contains_given_source() {
        val target = LlmLanguages.supportedLanguages.find { it.code == "ja" }!!
        val source = LlmLanguages.supportedLanguages.find { it.code == "en" }!!
        val system = TranslationPrompt.build(target = target, source = source)

        assertTrue(system.content.contains("from English (en)"))
        assertTrue(system.content.contains("Japanese (ja)"))
    }

    @Test
    fun prompt_resolves_source_language_code_without_falling_back_to_code_name() {
        val source = LlmLanguages.supportedLanguages.find { language -> language.code == "en" }!!
        val prompt = TranslationPrompt.build(
            target = LlmLanguages.supportedLanguages.find { language -> language.code == "ja" }!!,
            source = source
        )
        assertTrue(prompt.content.contains("from English (en)"))
    }

    @Test
    fun user_message_is_user_role() {
        val msg = TranslationPrompt.userMessage("hello")
        assertEquals("user", msg.role)
        assertEquals("hello", msg.content)
    }

    @Test
    fun request_serializes_model_and_stream_false() {
        val request = ChatCompletionRequest(
            model = "deepseek-chat",
            messages = listOf(ChatMessage("system", "s"), ChatMessage("user", "q")),
            stream = false
        )
        val json = JsonHelper.json.encodeToString(request)

        assertTrue(json.contains("\"model\":\"deepseek-chat\""))
        assertTrue(json.contains("\"stream\":false"))
        assertTrue(json.contains("\"role\":\"system\""))
    }

    @Test
    fun supported_languages_include_zh_cn_and_tw() {
        assertNotNull(LlmLanguages.supportedLanguages.find { it.code == "zh-rCN" })
        assertNotNull(LlmLanguages.supportedLanguages.find { it.code == "zh-rTW" })
    }
}
