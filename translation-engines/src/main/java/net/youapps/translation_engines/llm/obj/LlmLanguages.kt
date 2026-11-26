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

package net.youapps.translation_engines.llm.obj

import net.youapps.translation_engines.Language

object LlmLanguages {
    val supportedLanguages: List<Language> = listOf(
        Language(code = "en", name = "English"),
        Language(code = "ar", name = "Arabic"),
        Language(code = "az", name = "Azerbaijani"),
        Language(code = "be", name = "Belarusian"),
        Language(code = "bg", name = "Bulgarian"),
        Language(code = "bn", name = "Bengali"),
        Language(code = "ca", name = "Catalan"),
        Language(code = "cs", name = "Czech"),
        Language(code = "da", name = "Danish"),
        Language(code = "de", name = "German"),
        Language(code = "el", name = "Greek"),
        Language(code = "es", name = "Spanish"),
        Language(code = "et", name = "Estonian"),
        Language(code = "fa", name = "Persian"),
        Language(code = "fi", name = "Finnish"),
        Language(code = "fil", name = "Filipino"),
        Language(code = "fr-rFR", name = "French"),
        Language(code = "he", name = "Hebrew"),
        Language(code = "hi", name = "Hindi"),
        Language(code = "hu", name = "Hungarian"),
        Language(code = "ia", name = "Interlingua"),
        Language(code = "id", name = "Indonesian"),
        Language(code = "it", name = "Italian"),
        Language(code = "ja", name = "Japanese"),
        Language(code = "kab", name = "Kabyle"),
        Language(code = "ko", name = "Korean"),
        Language(code = "lt", name = "Lithuanian"),
        Language(code = "ml", name = "Malayalam"),
        Language(code = "ms", name = "Malay"),
        Language(code = "nb-rNO", name = "Norwegian Bokmål"),
        Language(code = "nl", name = "Dutch"),
        Language(code = "nn", name = "Norwegian Nynorsk"),
        Language(code = "or", name = "Odia"),
        Language(code = "pa", name = "Punjabi"),
        Language(code = "pa-rPK", name = "Punjabi (Pakistan)"),
        Language(code = "pl", name = "Polish"),
        Language(code = "pt", name = "Portuguese"),
        Language(code = "pt-rBR", name = "Portuguese (Brazil)"),
        Language(code = "ro", name = "Romanian"),
        Language(code = "ru", name = "Russian"),
        Language(code = "sat", name = "Santali"),
        Language(code = "sc", name = "Sardinian"),
        Language(code = "sr", name = "Serbian"),
        Language(code = "sv", name = "Swedish"),
        Language(code = "ta", name = "Tamil"),
        Language(code = "tr", name = "Turkish"),
        Language(code = "tt", name = "Tatar"),
        Language(code = "uk", name = "Ukrainian"),
        Language(code = "vi", name = "Vietnamese"),
        Language(code = "zh-rCN", name = "Chinese (Simplified)"),
        Language(code = "zh-rTW", name = "Chinese (Traditional)")
    )

    fun normalizeDetectedLanguage(code: String?): String? {
        val rawCode = code?.trim()?.replace('_', '-')?.takeIf { it.isNotEmpty() } ?: return null
        supportedLanguages.firstOrNull { it.code.equals(rawCode, ignoreCase = true) }?.let {
            return it.code
        }

        val components = rawCode.split('-').filter { it.isNotBlank() }
        val languageCode = components.firstOrNull()?.lowercase() ?: return null
        val region = components.drop(1)
            .firstOrNull { it.removePrefix("r").length == 2 }
            ?.removePrefix("r")
            ?.uppercase()

        if (languageCode == "zh") {
            return when {
                components.any { it.equals("Hans", ignoreCase = true) } || region == "CN" || region == "SG" -> "zh-rCN"
                components.any { it.equals("Hant", ignoreCase = true) } || region == "TW" || region == "HK" || region == "MO" -> "zh-rTW"
                else -> null
            }
        }

        val regionalCode = region?.let { "$languageCode-r$it" }
        return regionalCode?.let { candidate ->
            supportedLanguages.firstOrNull { it.code.equals(candidate, ignoreCase = true) }?.code
        } ?: supportedLanguages.firstOrNull { it.code.equals(languageCode, ignoreCase = true) }?.code
            ?: supportedLanguages.firstOrNull { it.code.startsWith("$languageCode-r", ignoreCase = true) }?.code
    }
}
