# CLAUDE.md

Instructions for Claude Code in this repo. `SPEC.md` is the source of truth; read it first.

## What this repo is

Branch `jf` of the public fork `freeman-jus/voice-keyboard`: a **builder** that applies two small patches to upstream `rustemar/voice-keyboard` release tags. It signs the result with Justin's key and publishes it as a GitHub Release for Obtainium. It contains no app source.

`archive/` holds a shelved plan to self-build Dictate Keyboard, kept for reference. It is gitignored and is not part of this project.

## Hard rules

1. **Never commit signing material or secrets.** Check `git status` before every commit.
2. **Never print secrets.** No `echo` of secret env vars; no `set -x` near them.
3. **Never modify upstream source except through `patches/`** (plus the R4 version stamp in CI). Two patches only; a new one needs Justin's approval.
4. **Never add network destinations, telemetry, permissions or dependencies** to the app.
5. **Keep attribution intact** (SPEC N5): README header, release-body attribution line, `LICENSE` in releases, "(JF)" app name.
6. **Keep the build/sign separation** (SPEC R8, N6). Only `sign` may reference `secrets.` or have `contents: write`.
7. **Never delete releases or tags.** Publish is draft → upload `--clobber` → publish.
8. **Pin every `uses:` to a full commit SHA**, version in a trailing comment.
9. **Ask Justin before:** pushing to `jf`, creating or publishing releases, changing secrets or variables, changing the schedule, adding a patch, or opening anything on rustemar's repo (issues, PRs).

## Regenerating a patch after upstream changes

```bash
git clone https://github.com/rustemar/voice-keyboard.git "$SCRATCH/vk" && cd "$SCRATCH/vk"
git checkout vX.Y.Z
git apply --3way /c/Dev/VTT/patches/001-*.patch   # resolve conflicts
# re-verify behaviour against SPEC R6, then regenerate:
git diff > /tmp/001.body   # prepend the existing header block, replace the file
```

## Useful commands

```bash
gh workflow run build-release.yml -f publish=false            # dry run
gh workflow run build-release.yml -f tag=v1.9.1               # build + publish a specific tag
gh run watch
apksigner verify --print-certs VoiceKeyboard-JF-*.apk
```
