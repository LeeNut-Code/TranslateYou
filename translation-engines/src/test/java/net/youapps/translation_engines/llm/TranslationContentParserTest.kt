package net.youapps.translation_engines.llm

import net.youapps.translation_engines.llm.obj.TranslationContentParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TranslationContentParserTest {

    private val parser = TranslationContentParser

    @Test
    fun parse_plain_json() {
        val result = parser.parse("""{"translation":"你好","detectedLanguage":"en"}""")
        assertEquals("你好", result.translatedText)
        assertEquals("en", result.detectedLanguage)
    }

    @Test
    fun parse_fenced_json() {
        val result = parser.parse("```json\n{\"translation\":\"こんにちは\",\"detectedLanguage\":\"en\"}\n```")
        assertEquals("こんにちは", result.translatedText)
        assertEquals("en", result.detectedLanguage)
    }

    @Test
    fun parse_fenced_json_no_lang() {
        val result = parser.parse("```\n{\"translation\":\"bonjour\"}\n```")
        assertEquals("bonjour", result.translatedText)
        assertNull(result.detectedLanguage)
    }

    @Test
    fun parse_plain_text_fallback() {
        val result = parser.parse("Hola mundo")
        assertEquals("Hola mundo", result.translatedText)
        assertNull(result.detectedLanguage)
    }

    @Test
    fun parse_json_without_detectedLanguage() {
        val result = parser.parse("""{"translation":"Ciao"}""")
        assertEquals("Ciao", result.translatedText)
        assertNull(result.detectedLanguage)
    }

    @Test
    fun parse_json_with_blank_translation_does_not_fall_back_to_raw_json() {
        val result = parser.parse("""{"translation":"  ","detectedLanguage":"en"}""")
        assertEquals("", result.translatedText)
        assertNull(result.detectedLanguage)
    }

    @Test
    fun parse_unknown_detected_language_as_unknown() {
        val result = parser.parse("""{"translation":"text","detectedLanguage":"xx-unknown"}""")
        assertEquals("text", result.translatedText)
        assertNull(result.detectedLanguage)
    }

    @Test
    fun normalize_regional_language_codes_to_app_codes() {
        val simplified = parser.parse("""{"translation":"你好","detectedLanguage":"zh-CN"}""")
        val traditional = parser.parse("""{"translation":"你好","detectedLanguage":"zh-TW"}""")
        assertEquals("zh-rCN", simplified.detectedLanguage)
        assertEquals("zh-rTW", traditional.detectedLanguage)
    }

    @Test
    fun parse_invalid_json_falls_back_to_text() {
        val result = parser.parse("not json at all {broken")
        assertEquals("not json at all {broken", result.translatedText)
        assertNull(result.detectedLanguage)
    }

    @Test
    fun parse_empty_content_returns_empty() {
        val result = parser.parse("")
        assertEquals("", result.translatedText)
        assertNull(result.detectedLanguage)
    }
}
