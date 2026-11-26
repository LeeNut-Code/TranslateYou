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

object TranslationPrompt {

    private const val SYSTEM_TEMPLATE = """
        You are a professional translator. Translate the user's text from the source language into the target language.
        Preserve paragraphs, line breaks and basic formatting as much as possible.
        Return a JSON object only, with exactly these fields:
        - "translation": the translated text (string)
        - "detectedLanguage": the detected source language code compatible with the app (string, or null if unknown)
        Do not wrap the JSON in markdown fences, and do not add any explanation or extra commentary.

        Source: {SOURCE}
        Target: {TARGET}
    """.trimIndent()

    fun build(target: Language, source: Language?): ChatMessage {
        val sourceLabel = source?.let { "from ${it.name} (${it.code})" } ?: "auto-detect"
        val system = SYSTEM_TEMPLATE
            .replace("{SOURCE}", sourceLabel)
            .replace("{TARGET}", "${target.name} (${target.code})")

        return ChatMessage(role = "system", content = system)
    }

    fun userMessage(query: String): ChatMessage = ChatMessage(role = "user", content = query)
}
