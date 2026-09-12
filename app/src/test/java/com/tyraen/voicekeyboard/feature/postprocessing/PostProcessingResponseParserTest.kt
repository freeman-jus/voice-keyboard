package com.tyraen.voicekeyboard.feature.postprocessing

import com.tyraen.voicekeyboard.feature.postprocessing.PostProcessingResponseParser.ProviderException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PostProcessingResponseParserTest {

    @Test fun `openai text is read and trimmed`() {
        val body = """{"choices":[{"index":0,"message":{"role":"assistant","content":"  Hello.  "},"finish_reason":"stop"}]}"""
        assertEquals("Hello.", PostProcessingResponseParser.openAiText(body))
    }

    @Test fun `openai content given as parts is joined`() {
        val body = """{"choices":[{"message":{"role":"assistant","content":[{"type":"text","text":"Hel"},{"type":"text","text":"lo"}]}}]}"""
        assertEquals("Hello", PostProcessingResponseParser.openAiText(body))
    }

    @Test fun `openai null content is an error not the word null`() {
        val body = """{"choices":[{"message":{"role":"assistant","content":null,"refusal":"I can't help with that."}}]}"""
        val e = assertThrows(ProviderException::class.java) { PostProcessingResponseParser.openAiText(body) }
        assertEquals("I can't help with that.", e.message)
    }

    @Test fun `openai null content without a refusal is still an error`() {
        val body = """{"choices":[{"message":{"role":"assistant","content":null}}]}"""
        val e = assertThrows(ProviderException::class.java) { PostProcessingResponseParser.openAiText(body) }
        assertEquals("Empty completion", e.message)
    }

    @Test fun `a 200 that only carries an error surfaces the provider's message`() {
        val body = """{"error":{"message":"Provider returned error","code":502,"metadata":{"provider_name":"OpenAI"}},"user_id":"u"}"""
        val e = assertThrows(ProviderException::class.java) { PostProcessingResponseParser.openAiText(body) }
        assertEquals("Provider returned error", e.message)
    }

    @Test fun `an html page is reported as such`() {
        val e = assertThrows(ProviderException::class.java) { PostProcessingResponseParser.openAiText("<!DOCTYPE html><html>404</html>") }
        assertEquals("Got a web page instead of an API response; check the endpoint URL", e.message)
    }

    @Test fun `claude text is read from the first text block`() {
        val body = """{"id":"msg","type":"message","role":"assistant","content":[{"type":"text","text":" OK "}],"stop_reason":"end_turn"}"""
        assertEquals("OK", PostProcessingResponseParser.claudeText(body))
    }

    @Test fun `claude with no text block is an error`() {
        val body = """{"id":"msg","type":"message","role":"assistant","content":[],"stop_reason":"end_turn"}"""
        assertThrows(ProviderException::class.java) { PostProcessingResponseParser.claudeText(body) }
    }

    @Test fun `error messages are found in the shapes providers actually use`() {
        assertEquals("No cookie auth credentials found", PostProcessingResponseParser.errorMessage("""{"error":{"message":"No cookie auth credentials found","code":401}}"""))
        assertEquals("invalid x-api-key", PostProcessingResponseParser.errorMessage("""{"type":"error","error":{"type":"authentication_error","message":"invalid x-api-key"}}"""))
        assertEquals("Not Found", PostProcessingResponseParser.errorMessage("""{"detail":"Not Found"}"""))
        assertEquals("plain error", PostProcessingResponseParser.errorMessage("""{"error":"plain error"}"""))
        assertNull(PostProcessingResponseParser.errorMessage("<!DOCTYPE html><html></html>"))
        assertNull(PostProcessingResponseParser.errorMessage("""{"choices":[]}"""))
        assertNull(PostProcessingResponseParser.errorMessage("not json at all"))
    }

    @Test fun `validation failures quote the provider and point at both likely causes of a 404`() {
        assertEquals(
            "Not found: check the endpoint URL and the model name (gpt-4o-mini is not a valid model ID)",
            PostProcessingClient.describeFailure(404, """{"error":{"message":"gpt-4o-mini is not a valid model ID","code":404}}""")
        )
        assertEquals("Invalid API key", PostProcessingClient.describeFailure(401, "<!DOCTYPE html>"))
        assertEquals("Server error, try again later", PostProcessingClient.describeFailure(503, ""))
        assertEquals("API error 418 (teapot)", PostProcessingClient.describeFailure(418, """{"error":{"message":"  teapot\n"}}"""))
    }
}
