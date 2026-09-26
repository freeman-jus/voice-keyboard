# Voice Keyboard (JF): unofficial fork

> **Retired (26 Sep 2026).** This fork's feature was merged upstream in [rustemar/voice-keyboard#5](https://github.com/rustemar/voice-keyboard/pull/5) and shipped in **Voice Keyboard v1.9.2**, as the "Return to previous keyboard" setting. Please use the [official releases](https://github.com/rustemar/voice-keyboard/releases). This repo is archived; the old builds remain for reference only.

> **This is an unofficial fork of [Voice Keyboard](https://github.com/rustemar/voice-keyboard) by [rustemar](https://github.com/rustemar).**
> All of the app is rustemar's work, under the MIT licence. This fork adds one opt-in setting and builds signed APKs from rustemar's release tags. It is not affiliated with or endorsed by rustemar.
> **Please report problems with these builds here, not upstream.** If you just want Voice Keyboard, use the [official releases](https://github.com/rustemar/voice-keyboard/releases).

## What's different

| Patch | Change |
|---|---|
| `001-return-to-previous-keyboard` | New setting, **"Return to previous keyboard after inserting"** (off by default). When your keyboard's mic key hands off to Voice Keyboard, it switches back to that keyboard once the text is in. It waits until nothing is still recording or queued, and never switches back after a failed recording. |
| `002-side-by-side-identity` | Package `com.tyraen.voicekeyboard.jf`, name "Voice Keyboard (JF)", so it installs alongside the official app instead of replacing it. |

Nothing else is changed. Each release is built automatically from an upstream release tag (`vX.Y.Z` → `vX.Y.Z-jf.N`). Its notes show the permissions, dependencies and domains that changed since the previous build. That's a change summary, not an audit.

This branch (`jf`) holds only the build tooling: `SPEC.md`, `patches/` and `.github/workflows/`. The fork's `main` branch is upstream's code as it was when the fork was made.

## Install (Android, via Obtainium)

1. Obtainium → Add app → `https://github.com/freeman-jus/voice-keyboard`.
2. Install "Voice Keyboard (JF)". The official Voice Keyboard can stay installed.
3. Open it and set it up as you would the official app. Settings don't transfer (the app has no export), so copy them across with both apps open.
4. Turn on **"Return to previous keyboard after inserting"**.
5. Leave **"Check for updates" off**. That checker installs the *official* app; Obtainium updates this one.

## Phone setup (GrapheneOS + HeliBoard)

1. Settings → System → Keyboard → On-screen keyboard: enable **Voice Keyboard (JF)**. Disable the official Voice Keyboard's entry and any other voice input (FUTO, Whisper+, Transcribro, OpenVoiceIME). HeliBoard's mic uses the first enabled voice input it finds and offers no choice.
2. App permissions: microphone and network allowed.
3. **Test:**
   - Tap HeliBoard's mic, dictate, and check HeliBoard comes back on its own.
   - Queue two recordings; it should return only after the second insert.
   - In airplane mode, the failed recording should stay on screen for retry.

## Maintainer setup (one-off)

1. **Signing key** (on your own machine; back it up offline with its password):
   ```bash
   keytool -genkeypair -v -keystore vk-jf.jks -storetype PKCS12 -alias vkjf \
     -keyalg RSA -keysize 4096 -validity 36500 -dname "CN=freeman-jus personal build"
   keytool -list -v -keystore vk-jf.jks -alias vkjf | grep SHA256:
   ```
2. **Secrets** (you type the password; it never passes through anyone else):
   ```bash
   base64 -w0 vk-jf.jks | gh secret set JF_KEYSTORE_BASE64 -R freeman-jus/voice-keyboard
   gh secret set JF_KEYSTORE_PASSWORD -R freeman-jus/voice-keyboard
   gh secret set JF_KEY_ALIAS -R freeman-jus/voice-keyboard -b vkjf
   gh variable set EXPECTED_CERT_SHA256 -R freeman-jus/voice-keyboard -b "<SHA256 from step 1>"
   ```
3. Actions → "Build Voice Keyboard (JF)" → Run workflow.

## Maintenance

- **Normal state:** nothing to do. New upstream releases build within a day, and Obtainium offers the update.
- **Build fails** (usually a patch no longer applies after an upstream change): the phone keeps the last build. Regenerate the patch (see `CLAUDE.md`).
- **If upstream adds this feature:** retire the fork and switch back to the official app.

## Licence

MIT. See [LICENSE](LICENSE). Voice Keyboard © 2026 rustemar. Build tooling and patches © 2026 freeman-jus.
