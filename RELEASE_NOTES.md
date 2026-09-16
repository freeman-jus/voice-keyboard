v1.9.0 — Smarter spacing, sturdier recording, easier setup

- Dictated text gets a space in front when the cursor sits after a word, and never a doubled one before an existing space or full stop; the `.` `?` `!` keys now swallow the space dictation leaves behind, so "hello " + `.` reads "hello."
- A recording stopped right before switching keyboards or changing the theme could be deleted while it was still uploading; it now always reaches the queue
- An accidental double tap on the mic no longer produces an unsendable recording with a permanent red badge; captures too short to contain speech are dropped
- Recordings that failed permanently can be deleted from the keyboard: hold the resend key, then hold it again while the hint is shown
- A busy or unavailable microphone shows a message instead of crashing the keyboard
- Uploads are three times smaller (Opus 64 kbps): faster results on mobile data, fewer timeouts
- Tapping the mic without microphone permission now opens the permission prompt right from the keyboard; after "Don't ask again" it opens the app's settings page
- The settings screen shows whether the keyboard is enabled and offers "Enable keyboard" and "Switch keyboard" buttons, plus a hint when Voice Keyboard is the active keyboard and its panel cannot type into the fields
- Provider presets on both settings screens fill in the address and a model that exists there (Groq, OpenAI, Mistral for speech; OpenAI, Claude, OpenRouter, Groq, Mistral, DeepSeek for post-processing)
- Reasoning models work with post-processing: their thinking blocks are removed from the output, and models that reject a custom temperature are retried without it; the temperature setting now applies to Claude as well
- A recording made before an API key is entered is kept with the red badge instead of being lost; enter the key and tap resend
- Prompt improvements now reach everyone: pressing Apply no longer pins you to the prompt wording of that version
- A wrong or retired speech-to-text model name is reported with the provider's own message instead of "check the URL"
- Empty post-processing results no longer insert a stray space or overwrite the clipboard
- Logs and crash reports can be shared straight from settings and carry the app version
- Keyboard status messages and setup-screen texts are translated in all 17 languages
- Release builds without signing credentials produce an unsigned APK instead of failing, and the Play-only dependency metadata block is no longer embedded

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
