# patches/

`git apply`-able patches, applied in filename order to a clean upstream release tag before building.

Each patch file starts with a header block (plain text before the first `diff --git` line, which `git apply` ignores) stating:

- **What** it changes
- **Why** it's needed (SPEC.md requirement ID)
- **When** it can be deleted (e.g. "once upstream ships an equivalent setting")

Rules:

- Generated with `git diff` from a clean checkout of the upstream tag. Paths are relative to the upstream repo root.
- No new network destinations, telemetry, permissions or dependencies, ever.
- Only `001` and `002` exist by design. Adding a patch needs Justin's approval.
