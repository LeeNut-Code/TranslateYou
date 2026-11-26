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

import net.youapps.translation_engines.JsonHelper
import kotlinx.serialization.Serializable

private const val FENCE_OPEN = "```"

/**
 * Parses the model response content into a translation and an optional detected language code.
 *
 * Accepts:
 *   1. Plain JSON object with `translation` (and optional `detectedLanguage`).
 *   2. Markdown fenced JSON (```json ... ```).
 *   3. Plain text fallback: the whole trimmed content is treated as the translation.
 *
 * Never throws on unparseable content; falls back to the raw text as the translation.
 */
object TranslationContentParser {

    @Serializable
    private data class ParsedJson(
        val translation: String? = null,
        val detectedLanguage: String? = null
    )

    data class Result(
        val translatedText: String,
        val detectedLanguage: String? = null
    )

    fun parse(content: String): Result {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return Result(translatedText = "")
        }

        val jsonCandidate = extractJson(trimmed)
        jsonCandidate?.let { json ->
            return runCatching {
                JsonHelper.json.decodeFromString<ParsedJson>(json)
            }.getOrNull()?.let { parsed ->
                val text = parsed.translation?.trim().orEmpty()
                Result(
                    translatedText = text,
                    detectedLanguage = LlmLanguages.normalizeDetectedLanguage(parsed.detectedLanguage)
                )
            } ?: Result(translatedText = trimmed)
        }

        return Result(translatedText = trimmed)
    }

    private fun extractJson(text: String): String? {
        val fenceIndex = text.indexOf(FENCE_OPEN)
        if (fenceIndex < 0) {
            return if (text.trimStart().startsWith("{")) text else null
        }

        val afterOpen = text.substring(fenceIndex + FENCE_OPEN.length)
        val newlineIndex = afterOpen.indexOf('\n')
        val bodyStart = if (newlineIndex >= 0) newlineIndex + 1 else afterOpen.length
        val closeIndex = afterOpen.indexOf(FENCE_OPEN, bodyStart)
        val body = if (closeIndex >= 0) afterOpen.substring(0, closeIndex) else afterOpen.substring(bodyStart)

        return body.takeIf { it.trimStart().startsWith("{") }
    }
}
