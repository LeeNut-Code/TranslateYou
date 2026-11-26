package com.bnyro.translate.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageNamesTest {

    @Test
    fun chinese_display_combines_localized_and_native_name() {
        assertEquals("英语 (English)", LanguageNames.displayName("en", "zh-rCN", "English"))
        assertEquals("日语 (日本語)", LanguageNames.displayName("ja", "zh-rCN", "Japanese"))
        assertEquals("中文（简体） (简体中文)", LanguageNames.displayName("zh-rCN", "zh-rCN", "Chinese (Simplified)"))
    }

    @Test
    fun traditional_chinese_preference_is_also_localized() {
        assertEquals("英语 (English)", LanguageNames.displayName("en", "zh-rTW", "English"))
    }

    @Test
    fun unknown_code_falls_back_to_engine_name() {
        assertEquals("Klingon", LanguageNames.displayName("tlh", "zh-rCN", "Klingon"))
        assertNull(LanguageNames.localizedName("tlh", "zh-rCN"))
    }

    @Test
    fun non_chinese_environment_falls_back_to_engine_name() {
        assertEquals("English", LanguageNames.displayName("en", "de", "English"))
        assertEquals("English", LanguageNames.displayName("en", null, "English"))
        assertNull(LanguageNames.localizedName("en", "de"))
    }

    @Test
    fun empty_pref_and_chinese_system_tag_is_chinese() {
        assertTrue(LanguageNames.isChinese("", "zh-Hans-CN"))
    }

    @Test
    fun explicit_non_chinese_pref_wins_over_system_tag() {
        assertFalse(LanguageNames.isChinese("de", "zh-Hans-CN"))
    }

    @Test
    fun chinese_pref_makes_is_chinese_true() {
        assertTrue(LanguageNames.isChinese("zh-rCN", "en-US"))
    }

    @Test
    fun matches_code_engine_and_chinese_names() {
        assertTrue(LanguageNames.matches("en", "English", "zh-rCN", "英语"))
        assertTrue(LanguageNames.matches("en", "English", "zh-rCN", "english"))
        assertTrue(LanguageNames.matches("en", "English", "zh-rCN", "en"))
        assertTrue(LanguageNames.matches("ja", "Japanese", "zh-rCN", "日本語"))
        assertFalse(LanguageNames.matches("en", "English", "zh-rCN", "日语"))
    }

    @Test
    fun blank_query_matches_everything() {
        assertTrue(LanguageNames.matches("en", "English", "zh-rCN", "  "))
    }

    @Test
    fun underscore_language_tag_is_treated_as_chinese() {
        assertEquals("英语 (English)", LanguageNames.displayName("en", "zh_CN", "English"))
    }
}
