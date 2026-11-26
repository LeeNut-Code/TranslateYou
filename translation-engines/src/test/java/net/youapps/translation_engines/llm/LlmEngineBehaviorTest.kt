package net.youapps.translation_engines.llm

import kotlinx.coroutines.runBlocking
import net.youapps.translation_engines.ApiKeyState
import net.youapps.translation_engines.EngineSettingsProvider
import net.youapps.translation_engines.TranslationEngine
import net.youapps.translation_engines.TranslationEngines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSettingsProvider : EngineSettingsProvider {
    override fun getApiUrl(engine: TranslationEngine): String? = null
    override fun getApiKey(engine: TranslationEngine): String? = null
    override fun getSelectedModel(engine: TranslationEngine): String? = null
}

class LlmEngineBehaviorTest {

    private val engine = LlmEngine(FakeSettingsProvider())

    @Test
    fun capabilities() {
        assertEquals("LLM (OpenAI-compatible)", engine.name)
        assertEquals("https://api.openai.com/v1/", engine.defaultUrl)
        assertTrue(engine.urlModifiable)
        assertEquals(ApiKeyState.OPTIONAL, engine.apiKeyState)
        assertTrue(engine.supportedModels.isEmpty())
        assertTrue(engine.supportsCustomModelId)
        assertTrue(engine.supportsLanguageDetection)
    }

    @Test
    fun translate_requires_model_id() {
        val failure = runCatching {
            runBlocking { engine.translate("hello", null, "en") }
        }.exceptionOrNull()
        assertTrue(failure is IllegalStateException)
        assertTrue(failure?.message.orEmpty().contains("Model ID"))
    }

    @Test
    fun default_url_trailing_slash() {
        assertTrue(engine.defaultUrl.endsWith("/"))
    }

    @Test
    fun registration_preserves_existing_default_engine() {
        val engines = TranslationEngines.getAllEngines(FakeSettingsProvider())
        assertEquals("Mozhi", engines.first().name)
        assertEquals("LLM (OpenAI-compatible)", engines.last().name)
    }
}
