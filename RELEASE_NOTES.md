v1.8.9 — OpenRouter and other OpenAI-compatible providers made easy

- Post-processing accepts a provider's base URL (e.g. https://openrouter.ai/api/v1) and completes the path automatically; the speech-to-text endpoint gets the same treatment, and a missing https:// is added
- The provider selector now reads "OpenAI-compatible (OpenAI, OpenRouter, Groq…)" and follows the pasted address when it clearly belongs to the other provider
- Validation errors show the provider's own message, so a wrong model name is no longer reported as a wrong URL
- With a custom endpoint, translation reuses your model instead of a default that only exists on the official API
- Hints in settings and the README explain the vendor-prefixed model names gateways require, and that modes are toggled on the keyboard itself
- A null completion from the provider no longer types the word "null" into the text field

v1.8.8 — Period key on the keyboard

- A period key sits next to the space bar, alongside the existing `?` and `!`, so you can add a full stop without switching keyboards

v1.8.7 — Switch dictation language on the keyboard

- Set several dictation languages in settings ("ru, en, de") and a language key appears on the keyboard next to the space bar: tap it to cycle through them, long-press to pick one from a list
- The chosen language sticks between sessions, and the formatting prompt follows it, so switching to another language no longer leaves the recognizer biased toward the previous one
- Nothing changes if you dictate in a single language: the key stays hidden and the space bar keeps its full width

v1.8.6 — Clearer setup and fuller translations

- The settings screen no longer implies you need a Groq key specifically: the field is now labelled "Speech-to-text API key" and links to free keys from both Groq and Mistral, with the exact endpoint and model to use
- Finished translating the interface: the "Rhyme" mode name and several button labels that were still showing in English are now translated in every supported language
