# SPEC: Voice Keyboard (JF) — auto-return fork

Status: approved 2026-09-25 · Owner: Justin (`freeman-jus`) · Architect: Claude Code

---

## 1. Purpose

Maintain a small, clearly attributed fork of **Voice Keyboard** by rustemar (`github.com/rustemar/voice-keyboard`, MIT). It adds one feature: an **opt-in "return to previous keyboard after inserting"** setting.

It is used as HeliBoard's voice input on a GrapheneOS Pixel:

- HeliBoard's mic key hands off to Voice Keyboard (JF).
- Voice Keyboard transcribes (Groq, own key), post-processes, and inserts the text.
- It then **returns to HeliBoard automatically**.

Upstream is excellent apart from that missing hand-back. This fork exists only to add it, until or unless upstream adopts it.

Builds are produced automatically from **upstream release tags** and signed with Justin's key. They install via Obtainium **side by side** with the official app.

## 2. Context and constraints

| Item | Value |
|---|---|
| Upstream | `rustemar/voice-keyboard`, MIT, © 2026 rustemar |
| Upstream release tags | `^v[0-9]+\.[0-9]+\.[0-9]+$` (e.g. `v1.9.1`), published about weekly, each with a `VoiceKeyboard.apk` |
| Upstream commit signing | **None** (`git log %G?` = `N`). No GPG pinning is possible. Trust in rustemar's GitHub account is the same as installing his APK today. |
| Build | Single `:app` module, Gradle, **JDK 17**, no NDK, no Google Play Services. Upstream `build.yml` uses `./gradlew testDebugUnitTest` then `./gradlew assembleRelease`. |
| Unsigned release | Upstream's `signingConfig` is skipped when `KEYSTORE_PASSWORD`, `KEY_ALIAS` or `KEY_PASSWORD` is blank or the keystore is missing (`app/build.gradle.kts`). **Do not set them in the build job** → unsigned output at `app/build/outputs/apk/release/VoiceKeyboard.apk` (`outputFileName` override). Confirm on the first CI run. |
| This repo | **Public GitHub fork** `freeman-jus/voice-keyboard` (the "forked from" banner is intentional). Default branch **`jf`**: an orphan branch holding only builder files (this spec, workflow, patches, docs). Upstream source is never committed to `jf`. The fork's `main` is left as upstream's and is not synced. |
| Install channel | Obtainium → this repo's GitHub Releases (public, no token needed) |
| Device | Pixel, GrapheneOS, no Play Services; typing keyboard HeliBoard |

## 3. Architecture

```
 rustemar/voice-keyboard               freeman-jus/voice-keyboard  (branch jf)
 ───────────────────────               ───────────────────────────────────────────
 release tag vX.Y.Z  ────────────────▶ job "check"  contents:read, actions:write (keepalive only)
                                          resolve newest vX.Y.Z; skip if a vX.Y.Z-jf.* release exists
                                                │
                                                ▼
                                       job "build"  contents:read, NO secrets, no git creds
                                          checkout upstream @ tag commit; git apply patches/*.patch
                                          stamp versionName X.Y.Z-jf.N
                                          ./gradlew testDebugUnitTest; ./gradlew :app:assembleRelease (unsigned)
                                          aapt2 permissions, dependency list, domain grep
                                          upload artifact + record SHA-256
                                                │  (artifact only; skipped when publish=false)
                                                ▼
                                       job "sign"   contents:write, keystore secrets, runs NO upstream code
                                          verify digest; zipalign + apksigner; cert fingerprint guard
                                          change summary + attribution; draft → upload → publish
                                                │
                                                ▼
                                       Obtainium on the phone
```

**Builder, not a merged source branch.** The patches are small, and upstream ships weekly. Applying `git apply` to pristine tags keeps every build traceable to an exact upstream commit plus two readable patch files. A patch that stops applying fails the build loudly, and the phone keeps the last good version.

## 4. Functional requirements

**R1 Detection (daily).**
- Resolve the newest non-draft, non-prerelease upstream release whose tag matches `^v[0-9]+\.[0-9]+\.[0-9]+$`.
- If any release in this repo already has a tag beginning `<thatTag>-jf.`, exit cleanly without building.

**R2 Manual trigger.** `workflow_dispatch` takes two inputs:
- `tag` (optional; blank = newest). A dispatch always builds, even if that tag was built before. This is how a patch fix ships as a new `-jf.N`.
- `publish` (boolean, default `true`). When `false`, only `check` and `build` run: a dry run that proves the patches apply, the tests pass and the build succeeds.

**R3 Patches.** From the upstream checkout root, apply `patches/*.patch` in filename order with `git apply --verbose`. Any reject fails the job.

**R4 Version stamp.**
- Before building, rewrite `versionName = "X.Y.Z"` to `versionName = "X.Y.Z-jf.N"` in the working tree's `app/build.gradle.kts`, where `N = github.run_number`.
- Fail if the substitution doesn't happen exactly once.
- Leave `versionCode` unchanged.
- This is a build-time stamp, not a source patch.
- The release tag is `vX.Y.Z-jf.N`. The APK's `versionName` must equal the tag without the leading `v`, so Obtainium's version check matches.

**R5 Build.**
1. `./gradlew testDebugUnitTest --no-daemon`
2. `./gradlew :app:assembleRelease --no-daemon --stacktrace`

Use JDK 17 (Temurin) and `runs-on: ubuntu-24.04`. No signing environment variables.

**R6 Patch `001-return-to-previous-keyboard.patch` (the feature).** It must be upstream-quality, since it may be offered to rustemar as a PR. The behaviour:

- **Setting.** A new boolean preference, **default `false`**, persisted like the existing auto-record preference (`core/config/PreferenceStore.kt`). Add a `Switch` on the setup screen next to `switchAutoRecord` (`res/layout/activity_setup.xml`, wired in `feature/setup/SetupActivity.kt`), labelled **"Return to previous keyboard after inserting"**. Add a short description if the neighbouring switches have one. English string in `res/values/strings.xml`.
- **When it switches back:** only when all of these hold:
  - the setting is on;
  - a dictation result was just **successfully inserted** into the current `InputConnection` (the `onTextReady` path in `feature/ime/DictationInputMethod.kt` where `insertDictation` returns true, not the clipboard fallback);
  - nothing is recording or capturing;
  - nothing is still pending in the processing queue;
  - the failed or parked count is zero.
- **With several recordings queued:** switch after the **last** one is inserted, not the first.
- **When it never switches:**
  - text went to the clipboard fallback;
  - any error, failed or parked recording (the retry UI must stay visible);
  - cancel;
  - the setting is off.
- **How it switches:** `switchToPreviousInputMethod()` on API 28+ (minSdk is 24). If that returns false or is unavailable, call `requestHideSelf(0)`. Run it on the main thread.
- **Ordering hazard:** read `feature/ime/ProcessingQueue.kt` to see how and when the pending count and `onQueueCountChanged` update relative to `deliver()`. Base the decision on state that already reflects the item just delivered: query the queue directly, or post the evaluation to the main looper after delivery. Document the choice in a one-line comment.
- **Testability:** put the decision in a small pure function (for example `shouldReturnToPreviousKeyboard(enabled, inserted, capturing, pendingCount, failedCount): Boolean`) in its own file under `feature/ime/`. Add a JVM unit test under `app/src/test/...` covering the truth table.
- **Style and footprint:** match upstream style (Kotlin, naming, comment density). No new dependencies, permissions or network calls. Minimal diff.

**R7 Patch `002-side-by-side-identity.patch`.**
- In `app/build.gradle.kts` `defaultConfig`, add `applicationIdSuffix = ".jf"`, giving the package `com.tyraen.voicekeyboard.jf`.
- In `res/values/strings.xml`, set `app_name` to **"Voice Keyboard (JF)"**.
- Nothing else: namespace, classes and `method.xml` stay unchanged. The code uses `packageName` dynamically; there are no hard-coded package strings, which was verified at v1.9.1.

**R8 Signing, isolated.**
- A separate `sign` job that runs no upstream code does the signing.
- Tools: `zipalign -p -f 4`, then `apksigner` with v2 and v3 schemes on (v1 off).
- Keystore from secrets `JF_KEYSTORE_BASE64` and `JF_KEYSTORE_PASSWORD` (PKCS12: key password = store password), alias `JF_KEY_ALIAS`, decoded to `$RUNNER_TEMP` and shredded afterwards.
- The **`JF_` prefix is deliberate.** Upstream's own `build.yml` on the fork's `main` references `KEYSTORE_*` secrets and must never be able to see ours.
- The build job has `contents: read`, no `secrets.` references, and `persist-credentials: false` on every checkout. Only the artifact crosses to `sign`, and its SHA-256 is re-verified there.

**R9 Fingerprint guard.** After signing, compare the signer cert SHA-256 with the repo variable `EXPECTED_CERT_SHA256` (colons and case tolerated). Fail on mismatch before anything is published.

**R10 Change summary and attribution.**
- Attach `permissions.txt` (`aapt2 dump permissions` on the finished APK), `dependencies.txt` (`:app:dependencies --configuration releaseRuntimeClasspath`) and `domains.txt` (an indicative `http(s)://` grep of the patched working tree).
- Diff them against the previous release in the body. Label it a change summary, not an audit.
- **The release body must open with attribution:**
  > Unofficial build of [Voice Keyboard](https://github.com/rustemar/voice-keyboard) by **rustemar** (MIT), upstream `vX.Y.Z` ([release notes](…)), plus the patches listed below. Not affiliated with or endorsed by rustemar. Please report problems with this build here, not upstream.
- Then list the applied patch filenames and their one-line purposes, and the signer SHA-256.

**R11 Publish, staged.**
- Create or reuse a **draft** titled `Voice Keyboard vX.Y.Z-jf.N (unofficial fork build)`.
- Upload with `--clobber`: `VoiceKeyboard-JF-vX.Y.Z-jf.N.apk`, the three summary files and `LICENSE` (upstream's MIT licence text, copied from the upstream checkout via the artifact).
- Then publish.
- Target the `jf` branch for the tag.

**R12 Never destructive.** No step deletes a release or tag. A failed run leaves existing releases untouched.

**R13 Keepalive.** Public repos auto-disable scheduled workflows after 60 days of repository inactivity. At the end of each run, the `check` job calls `PUT /repos/{owner}/{repo}/actions/workflows/{this workflow}/enable` with `actions: write`. Only `check` gets that permission.

## 5. Non-functional requirements

- **N1 No added egress.** Patches never add network destinations, analytics, crash reporting, permissions or dependencies.
- **N2 Secrets hygiene.** No secret is echoed or placed in `set -x` scope. The keystore lives only in `$RUNNER_TEMP` and is never uploaded.
- **N3 Two patches.** Only 001 and 002. Any new patch needs Justin's approval and follows `patches/README.md`.
- **N4 Pinned tooling.** Every `uses:` is pinned to a full commit SHA with the version in a trailing comment. `runs-on: ubuntu-24.04`.
- **N5 Attribution everywhere.**
  - The README's first lines say this is an **unofficial fork** of rustemar's Voice Keyboard, with credit and links.
  - The MIT licence is retained, and `LICENSE` ships with every release.
  - The app name carries "(JF)".
  - The release bodies open with the attribution line (R10).
- **N6 Least privilege.** Permissions are set per job: `check` has `contents: read` and `actions: write`; `build` has `contents: read`; `sign` has `contents: write`.
- **N7 Honest claims.** Say nothing implying this build is more private or secure than upstream. It exists for one feature.

## 6. Implementation plan

1. **Phase 1, build locally.** Architect writes the docs; implementers write:
   - the patches: generated with `git diff` against a clean `v1.9.1` checkout, each opening with the header block from `patches/README.md`, and checked with `git apply --check`;
   - the workflow: adapted from `archive/dictate-build/.github/workflows/build-release.yml`, with the GPG step, native-library step and ABI stripping removed.

   There's no local Android SDK, so compiling happens in CI.
2. **Phase 2, publish the fork** (Justin approved a public fork on 2026-09-25):
   - `gh repo fork rustemar/voice-keyboard --clone=false`;
   - push `jf`, set it as the default branch;
   - enable Actions, then **disable upstream's "Build & Release APK" workflow**;
   - add a repo description, e.g. "Unofficial fork of rustemar/voice-keyboard adding return-to-previous-keyboard".
   - Then dispatch with `publish=false` (a dry run) and fix until green.
3. **Phase 3, key and first release.**
   - **Justin:** creates the keystore, sets the secrets and the variable (README "Maintainer setup").
   - **Architect:** dispatches `publish=true` and verifies both guards, the attribution and the assets.
4. **Phase 4, phone (Justin).** Install via Obtainium and set up side by side (README "Phone setup"). Then run the acceptance tests.
5. **Phase 5 (optional, later).** Offer patch 001 to rustemar as a PR, on a branch cut from upstream `main` in this fork. If it's merged, retire the fork and move back to the official app.

## 7. Acceptance criteria

- [ ] A dry run (`publish=false`) is green: patches apply, upstream and new unit tests pass, unsigned APK built.
- [ ] A release `vX.Y.Z-jf.N` exists with the APK, `permissions.txt`, `dependencies.txt`, `domains.txt` and `LICENSE`, and its body opens with the attribution line.
- [ ] `apksigner verify --print-certs` shows Justin's cert, matching `EXPECTED_CERT_SHA256`. The package is `com.tyraen.voicekeyboard.jf`; the label is "Voice Keyboard (JF)"; `versionName` equals the tag without the `v`.
- [ ] The build job has no `secrets.` references and `contents: read` only; every `uses:` is SHA-pinned.
- [ ] A scheduled run with no new upstream tag exits without building.
- [ ] A bad keystore password or wrong `EXPECTED_CERT_SHA256` fails without publishing and without touching existing releases.
- [ ] **On the phone:** HeliBoard mic → Voice Keyboard (JF) → text inserted → **HeliBoard returns automatically** (setting on). With the setting off, it behaves exactly like upstream.
- [ ] **On the phone:** with two recordings queued, it returns after the second insert. In airplane mode, the failed recording stays visible with retry and there's no switch.
- [ ] The official Voice Keyboard remains installed and unaffected.

## 8. Out of scope

- Any other feature change.
- Tracking upstream `main` between releases.
- F-Droid or other distribution.
- Settings migration: the app has no export and blocks backup, so settings are re-entered once, by hand.

## 9. Risks

| Risk | Mitigation |
|---|---|
| An upstream refactor breaks a patch | The build fails and nothing is published; the phone keeps the last build. Regenerate the patch against the new tag. |
| The in-app update checker installs rustemar's **official** APK | Keep "check for updates" off in the JF build. With a different package, an accidental "update" installs or updates the official app, not this one. |
| Upstream's `build.yml` runs in the fork | It lives only on the fork's `main`, which is never pushed or synced, so it isn't registered (verified 2026-09-25). If it's ever registered, disable it. It could never see our `JF_*` secrets anyway. |
| Scheduled workflow auto-disabled (public repo) | R13 keepalive. `workflow_dispatch` always works. |
| Keystore lost | Updates become impossible; reinstall and re-enter settings. Keep an offline backup of the keystore and password. |
| Upstream adds a network feature | It shows in the change summary before you update. |
| rustemar objects to a public fork build | Respond promptly: make it private, rename, or withdraw. Offering the PR (Phase 5) is the constructive route. |
