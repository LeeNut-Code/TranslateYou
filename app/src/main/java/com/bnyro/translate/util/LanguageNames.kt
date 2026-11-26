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

package com.bnyro.translate.util

import java.util.Locale

/**
 * Localized display names for language codes.
 *
 * Engine [net.youapps.translation_engines.Language] objects carry English names. This object
 * formats them for the current app language without mutating the underlying data, so language
 * equality, bookmarks and history persistence are unaffected.
 *
 * Only Chinese environments are localized; other app languages fall back to the engine name.
 * The table is grouped per app language, so further locales can be added later without touching
 * UI code.
 */
object LanguageNames {

    private val zhCN: Map<String, Pair<String, String>> = mapOf(
        "en" to ("英语" to "English"),
        "ar" to ("阿拉伯语" to "العربية"),
        "az" to ("阿塞拜疆语" to "Azərbaycanca"),
        "be" to ("白俄罗斯语" to "Беларуская"),
        "bg" to ("保加利亚语" to "Български"),
        "bn" to ("孟加拉语" to "বাংলা"),
        "ca" to ("加泰罗尼亚语" to "Català"),
        "cs" to ("捷克语" to "Čeština"),
        "da" to ("丹麦语" to "Dansk"),
        "de" to ("德语" to "Deutsch"),
        "es" to ("西班牙语" to "Español"),
        "et" to ("爱沙尼亚语" to "Eesti"),
        "fa" to ("波斯语" to "فارسی"),
        "fi" to ("芬兰语" to "Suomi"),
        "fil" to ("菲律宾语" to "Filipino"),
        "fr-rFR" to ("法语" to "Français"),
        "he" to ("希伯来语" to "עברית"),
        "hi" to ("印地语" to "हिन्दी"),
        "hu" to ("匈牙利语" to "Magyar"),
        "ia" to ("国际语" to "Interlingua"),
        "id" to ("印度尼西亚语" to "Bahasa Indonesia"),
        "it" to ("意大利语" to "Italiano"),
        "ja" to ("日语" to "日本語"),
        "kab" to ("卡拜尔语" to "Taqbaylit"),
        "ko" to ("韩语" to "한국어"),
        "lt" to ("立陶宛语" to "Lietuvių"),
        "ml" to ("马拉雅拉姆语" to "മലയാളം"),
        "ms" to ("马来语" to "Bahasa Melayu"),
        "nb-rNO" to ("挪威语（书面）" to "Norsk bokmål"),
        "nl" to ("荷兰语" to "Nederlands"),
        "nn" to ("挪威语（新挪威语）" to "Norsk nynorsk"),
        "or" to ("奥里亚语" to "ଓଡ଼ିଆ"),
        "pa" to ("旁遮普语" to "ਪੰਜਾਬੀ"),
        "pa-rPK" to ("旁遮普语（巴基斯坦）" to "پنجابی"),
        "pl" to ("波兰语" to "Polski"),
        "pt" to ("葡萄牙语" to "Português"),
        "pt-rBR" to ("葡萄牙语（巴西）" to "Português (Brasil)"),
        "ro" to ("罗马尼亚语" to "Română"),
        "ru" to ("俄语" to "Русский"),
        "sat" to ("桑塔利语" to "ᱥᱟᱱᱛᱟᱲᱤ"),
        "sc" to ("撒丁语" to "Sardu"),
        "sr" to ("塞尔维亚语" to "Српски"),
        "sv" to ("瑞典语" to "Svenska"),
        "ta" to ("泰米尔语" to "தமிழ்"),
        "tr" to ("土耳其语" to "Türkçe"),
        "tt" to ("鞑靼语" to "Татарча"),
        "uk" to ("乌克兰语" to "Українська"),
        "vi" to ("越南语" to "Tiếng Việt"),
        "zh-rCN" to ("中文（简体）" to "简体中文"),
        "zh-rTW" to ("中文（繁体）" to "繁體中文")
    )

    private fun normalizeTag(tag: String): String = tag.replace('_', '-')

    private fun systemLanguageTag(): String = Locale.getDefault().toLanguageTag()

    /**
     * Whether the app is displaying in a Chinese locale.
     *
     * An explicit non-empty [appLanguagePref] wins; otherwise [systemLanguageTag] is used.
     */
    fun isChinese(appLanguagePref: String, systemLanguageTag: String): Boolean {
        val effective = appLanguagePref.takeIf { it.isNotBlank() } ?: systemLanguageTag
        return normalizeTag(effective).startsWith("zh", ignoreCase = true)
    }

    private fun isChinese(appLanguage: String?): Boolean =
        isChinese(appLanguage.orEmpty(), systemLanguageTag())

    /** Chinese localized name for [code], or null when not displayed in Chinese / unknown code. */
    fun localizedName(code: String, appLanguage: String?): String? {
        if (!isChinese(appLanguage)) return null
        return zhCN[code]?.first
    }

    /** Native (self) name for [code], or null when unknown. */
    fun nativeName(code: String): String? = zhCN[code]?.second

    /**
     * Human readable name for [code]. In a Chinese environment returns "中文名 (原生名)",
     * collapsing to a single label when they are equal or no native name exists, to avoid
     * redundancy. Otherwise falls back to [engineName].
     */
    fun displayName(code: String, appLanguage: String?, engineName: String): String {
        val localized = localizedName(code, appLanguage)
        val native = nativeName(code)
        return when {
            localized.isNullOrEmpty() -> engineName
            native.isNullOrEmpty() || localized == native -> localized
            else -> "$localized ($native)"
        }
    }

    /**
     * Whether [query] matches the code, engine name, localized name or native name of [code].
     * Used by the language picker search field so users can search in Chinese too.
     */
    fun matches(code: String, engineName: String, appLanguage: String?, query: String): Boolean {
        if (query.isBlank()) return true
        val needle = query.trim()
        if (code.contains(needle, ignoreCase = true)) return true
        if (engineName.contains(needle, ignoreCase = true)) return true
        if (!isChinese(appLanguage)) return false
        val localized = zhCN[code]?.first.orEmpty()
        val native = zhCN[code]?.second.orEmpty()
        return localized.contains(needle, ignoreCase = true) ||
            native.contains(needle, ignoreCase = true)
    }
}
