package net.youapps.translation_engines.llm

import kotlinx.coroutines.runBlocking
import net.youapps.translation_engines.EngineSettingsProvider
import net.youapps.translation_engines.TranslationEngine
import retrofit2.HttpException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class LlmEngineHttpTest {
    @Test
    fun sends_chat_completion_to_base_url_without_authorization_when_key_is_blank() {
        val server = MockWebServer()
        server.enqueue(jsonResponse("""{"choices":[{"message":{"role":"assistant","content":"{\"translation\":\"你好\",\"detectedLanguage\":\"en\"}"}}]}"""))
        server.start()
        try {
            val settings = TestSettings(
                apiUrl = server.url("/proxy/v1/").toString(),
                apiKey = "   ",
                model = "custom-model"
            )
            val engine = LlmEngine(settings).also { it.createOrRecreate() }
            val translation = runBlocking { engine.translate("hello", "en", "zh-rCN") }
            val request = server.takeRequest()

            assertEquals("POST", request.method)
            assertEquals("/proxy/v1/chat/completions", request.path)
            assertNull(request.getHeader("Authorization"))
            val body = request.body.readUtf8()
            assertTrue(body.contains("\"model\":\"custom-model\""))
            assertTrue(body.contains("\"stream\":false"))
            assertTrue(body.contains("\"role\":\"user\",\"content\":\"hello\""))
            assertEquals("你好", translation.translatedText)
            assertEquals("en", translation.detectedLanguage)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun sends_bearer_key_and_preserves_given_source_language() {
        val server = MockWebServer()
        server.enqueue(jsonResponse("""{"choices":[{"message":{"content":"Bonjour"}}]}"""))
        server.start()
        try {
            val settings = TestSettings(
                apiUrl = server.url("/compatible/v1/").toString(),
                apiKey = "test-secret",
                model = "model-123"
            )
            val engine = LlmEngine(settings).also { it.createOrRecreate() }
            runBlocking { engine.translate("Hello", "en", "fr-rFR") }
            val request = server.takeRequest()
            val body = request.body.readUtf8()

            assertEquals("Bearer test-secret", request.getHeader("Authorization"))
            assertTrue(body.contains("from English (en)"))
            assertTrue(body.contains("French (fr-rFR)"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun rejects_empty_choices_and_empty_message_content() {
        listOf(
            """{"choices":[]}""",
            """{"choices":[{"message":{"content":"  "}}]}""",
            """{"choices":[{"message":{"content":"{\"translation\":\"  \"}"}}]}"""
        ).forEach { responseBody ->
            val server = MockWebServer()
            server.enqueue(jsonResponse(responseBody))
            server.start()
            try {
                val engine = LlmEngine(
                    TestSettings(server.url("/v1/").toString(), null, "model")
                ).also { it.createOrRecreate() }
                val failure = runCatching {
                    runBlocking { engine.translate("hello", null, "en") }
                }.exceptionOrNull()

                assertTrue("Expected empty response to fail", failure is IllegalStateException)
            } finally {
                server.shutdown()
            }
        }
    }

    @Test
    fun rejects_blank_model_before_making_http_request() {
        val server = MockWebServer()
        server.start()
        try {
            val engine = LlmEngine(
                TestSettings(server.url("/v1/").toString(), null, "  ")
            ).also { it.createOrRecreate() }
            val failure = runCatching {
                runBlocking { engine.translate("hello", null, "en") }
            }.exceptionOrNull()

            assertTrue(failure is IllegalStateException)
            assertEquals(0, server.requestCount)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun propagates_http_status_for_authentication_errors() {
        val server = MockWebServer()
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"error":{"message":"unauthorized"}}""")
        )
        server.start()
        try {
            val engine = LlmEngine(
                TestSettings(server.url("/v1/").toString(), "fake-test-key", "model")
            ).also { it.createOrRecreate() }
            val failure = runCatching {
                runBlocking { engine.translate("hello", null, "en") }
            }.exceptionOrNull()

            assertTrue(failure is HttpException)
            assertEquals(401, (failure as HttpException).code())
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun llm_retrofit_client_does_not_log_source_text_or_authorization_key() {
        val server = MockWebServer()
        server.enqueue(jsonResponse("""{"choices":[{"message":{"content":"translated"}}]}"""))
        server.start()
        val logger = Logger.getLogger("okhttp3.OkHttpClient")
        val previousLevel = logger.level
        val previousParentHandlers = logger.useParentHandlers
        val messages = mutableListOf<String>()
        val handler = object : Handler() {
            override fun publish(record: LogRecord) {
                messages += record.message.orEmpty()
            }

            override fun flush() = Unit
            override fun close() = Unit
        }
        logger.addHandler(handler)
        logger.level = java.util.logging.Level.ALL
        logger.useParentHandlers = false
        try {
            val engine = LlmEngine(
                TestSettings(server.url("/v1/").toString(), "secret", "model")
            ).also { it.createOrRecreate() }
            runBlocking { engine.translate("private source", null, "en") }
            assertEquals(1, server.requestCount)
            assertTrue(messages.none { it.contains("private source") || it.contains("secret") })
        } finally {
            logger.removeHandler(handler)
            logger.level = previousLevel
            logger.useParentHandlers = previousParentHandlers
            server.shutdown()
        }
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    private class TestSettings(
        private val apiUrl: String?,
        private val apiKey: String?,
        private val model: String?
    ) : EngineSettingsProvider {
        override fun getApiUrl(engine: TranslationEngine): String? = apiUrl
        override fun getApiKey(engine: TranslationEngine): String? = apiKey
        override fun getSelectedModel(engine: TranslationEngine): String? = model
    }
}
