package com.tyraen.voicekeyboard.feature.postprocessing

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Pulls the generated text — or the provider's own error message — out of a chat-completion body.
 *
 * Everything is read defensively because gateways such as OpenRouter relay many upstream
 * providers: a 200 may carry only an `error` object when the upstream failed late, `content` may
 * be JSON null (refusal, nothing generated) or an array of parts, and on-device `org.json` turns a
 * null `getString` into the literal word "null", which would then be typed into the user's field.
 */
object PostProcessingResponseParser {

    class ProviderException(message: String) : Exception(message)

    /** Text of an OpenAI-style `chat/completions` response. */
    fun openAiText(body: String): String {
        val json = parse(body)
        val choices = json.optJSONArray("choices")
        if (choices == null || choices.length() == 0) {
            throw ProviderException(errorMessage(json) ?: "Response has no choices")
        }
        val message = choices.optJSONObject(0)?.optJSONObject("message")
            ?: throw ProviderException("Response has no message")
        return textOf(message.opt("content"))
            ?: throw ProviderException((message.opt("refusal") as? String)?.ifBlank { null } ?: "Empty completion")
    }

    /** Text of an Anthropic-style `messages` response. */
    fun claudeText(body: String): String {
        val json = parse(body)
        val content = json.optJSONArray("content")
        if (content == null || content.length() == 0) {
            throw ProviderException(errorMessage(json) ?: "Response has no content")
        }
        return textOf(content) ?: throw ProviderException("Empty completion")
    }

    /**
     * The human-readable message from an error body, if there is one. Covers
     * `{"error":{"message":…}}` (OpenAI, OpenRouter, Anthropic), `{"error":"…"}`, `{"message":…}`
     * and FastAPI's `{"detail":…}`. HTML and plain text yield null.
     */
    fun errorMessage(body: String): String? {
        val trimmed = body.trim()
        if (!trimmed.startsWith("{")) return null
        return try {
            errorMessage(JSONObject(trimmed))
        } catch (_: JSONException) {
            null
        }
    }

    private fun errorMessage(json: JSONObject): String? {
        val text = when (val error = json.opt("error")) {
            is JSONObject -> error.opt("message") as? String
            is String -> error
            else -> (json.opt("message") as? String) ?: (json.opt("detail") as? String)
        }
        return text?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun parse(body: String): JSONObject {
        val trimmed = body.trim()
        if (trimmed.startsWith("<")) {
            throw ProviderException("Got a web page instead of an API response; check the endpoint URL")
        }
        return try {
            JSONObject(trimmed)
        } catch (_: JSONException) {
            throw ProviderException("Response is not JSON: ${trimmed.take(80)}")
        }
    }

    /** A string as-is; an array of parts joined by their `text` fields; anything else null. */
    private fun textOf(content: Any?): String? = when (content) {
        is String -> content.trim()
        is JSONArray -> {
            val parts = (0 until content.length()).mapNotNull { i ->
                val part = content.optJSONObject(i) ?: return@mapNotNull null
                val type = part.opt("type") as? String ?: "text"
                if (type == "text") part.opt("text") as? String else null
            }
            if (parts.isEmpty()) null else parts.joinToString("").trim()
        }
        else -> null
    }
}
