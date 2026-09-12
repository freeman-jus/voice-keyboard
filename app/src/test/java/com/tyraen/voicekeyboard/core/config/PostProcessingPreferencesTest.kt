package com.tyraen.voicekeyboard.core.config

import com.tyraen.voicekeyboard.core.config.PostProcessingPreferences.Companion.PROVIDER_CLAUDE
import com.tyraen.voicekeyboard.core.config.PostProcessingPreferences.Companion.PROVIDER_OPENAI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PostProcessingPreferencesTest {

    @Test fun `blank endpoint resolves to the provider default`() {
        assertEquals(PostProcessingPreferences.DEFAULT_OPENAI_ENDPOINT, PostProcessingPreferences(provider = PROVIDER_OPENAI).resolvedEndpoint())
        assertEquals(PostProcessingPreferences.DEFAULT_CLAUDE_ENDPOINT, PostProcessingPreferences(provider = PROVIDER_CLAUDE).resolvedEndpoint())
    }

    @Test fun `a pasted base url is completed with the provider's request path`() {
        assertEquals(
            "https://openrouter.ai/api/v1/chat/completions",
            PostProcessingPreferences(provider = PROVIDER_OPENAI, endpoint = "https://openrouter.ai/api/v1").resolvedEndpoint()
        )
        assertEquals(
            "https://proxy.example/v1/messages",
            PostProcessingPreferences(provider = PROVIDER_CLAUDE, endpoint = "proxy.example/v1/").resolvedEndpoint()
        )
    }

    @Test fun `the request path names the provider`() {
        assertEquals(PROVIDER_OPENAI, PostProcessingPreferences.providerFor("https://openrouter.ai/api/v1/chat/completions"))
        assertEquals(PROVIDER_CLAUDE, PostProcessingPreferences.providerFor("https://proxy.example/v1/messages"))
    }

    @Test fun `a known host names the provider when the path does not`() {
        assertEquals(PROVIDER_OPENAI, PostProcessingPreferences.providerFor("https://openrouter.ai/api/v1"))
        assertEquals(PROVIDER_OPENAI, PostProcessingPreferences.providerFor("api.groq.com/openai/v1"))
        assertEquals(PROVIDER_CLAUDE, PostProcessingPreferences.providerFor("https://api.anthropic.com/v1"))
    }

    @Test fun `an unknown address or a blank one names nobody`() {
        assertNull(PostProcessingPreferences.providerFor("https://proxy.example/v1"))
        assertNull(PostProcessingPreferences.providerFor(""))
    }

    @Test fun `an explicit translation model always wins`() {
        val p = PostProcessingPreferences(provider = PROVIDER_OPENAI, endpoint = "https://openrouter.ai/api/v1", model = "openai/gpt-4o-mini", translateModel = "anthropic/claude-sonnet-4.6")
        assertEquals("anthropic/claude-sonnet-4.6", p.resolvedTranslateModel())
    }

    @Test fun `on the provider's own api translation uses the stronger default`() {
        assertEquals("gpt-4o", PostProcessingPreferences(provider = PROVIDER_OPENAI, model = "gpt-4o-mini").resolvedTranslateModel())
        assertEquals(
            "gpt-4o",
            PostProcessingPreferences(provider = PROVIDER_OPENAI, endpoint = "https://api.openai.com/v1", model = "gpt-4.1-mini").resolvedTranslateModel()
        )
        assertEquals("claude-sonnet-4-6", PostProcessingPreferences(provider = PROVIDER_CLAUDE).resolvedTranslateModel())
    }

    @Test fun `on a custom endpoint translation reuses the configured model`() {
        val p = PostProcessingPreferences(provider = PROVIDER_OPENAI, endpoint = "https://openrouter.ai/api/v1", model = "openai/gpt-4o-mini")
        assertEquals("openai/gpt-4o-mini", p.resolvedTranslateModel())
    }

    @Test fun `on a custom endpoint with no model at all the default is the only option left`() {
        val p = PostProcessingPreferences(provider = PROVIDER_OPENAI, endpoint = "https://openrouter.ai/api/v1")
        assertEquals("gpt-4o", p.resolvedTranslateModel())
    }
}
