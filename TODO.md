# TODO — when you're ready to publish

This is your checklist for taking Dhruva from "private repo on disk" to "available on Maven Central". Do these in order. Each is independent so you can pause between them.

## 1. Verify the namespace on Central Portal

You have DNS access for `ksharma.xyz`, so this is the canonical (branded) route.

- [ ] Sign in at <https://central.sonatype.com/>
- [ ] **Namespaces** → Add `xyz.ksharma`
- [ ] Copy the verification string Central gives you (looks like `_central=abc123xyz...`)
- [ ] Add a TXT record at the root of `ksharma.xyz`:
  - **Name**: `_central`
  - **Type**: `TXT`
  - **Value**: the verification string
- [ ] Wait ~5 minutes for DNS propagation
- [ ] Back on Central Portal: **Verify**
- [ ] Once verified, you own `xyz.ksharma:*` for everything you publish

## 2. Generate a PGP signing key

Maven Central requires every artifact to be signed.

- [ ] `brew install gnupg` if you don't already have it
- [ ] `gpg --gen-key` (pick a strong passphrase, save it somewhere safe like 1Password)
- [ ] `gpg --list-secret-keys --keyid-format=long` (note the long key ID after `sec rsa4096/`)
- [ ] `gpg --keyserver keyserver.ubuntu.com --send-keys <YOUR_KEY_ID>`
- [ ] Also send to: `gpg --keyserver keys.openpgp.org --send-keys <YOUR_KEY_ID>`
- [ ] Export the secret half for CI: `gpg --export-secret-keys --armor <YOUR_KEY_ID> > ~/dhruva-signing.asc`
- [ ] Keep `dhruva-signing.asc` somewhere safe and **never commit it**

## 3. Add 4 secrets to GitHub

Go to `https://github.com/ksharma-xyz/dhruva/settings/secrets/actions` and add:

- [ ] `SONATYPE_USERNAME` — from Central Portal → Account → Generate User Token (the "username" half)
- [ ] `SONATYPE_PASSWORD` — from same page (the "password" half)
- [ ] `SIGNING_KEY` — full ASCII-armored contents of `dhruva-signing.asc`
- [ ] `SIGNING_KEY_PASSWORD` — the passphrase you set in step 2

Repeat for `https://github.com/ksharma-xyz/dhruva/settings/secrets/actions` (same values, both repos use the same key).

## 4. Cut v0.1.0

When you're confident the API is right:

- [ ] Bump version: in `gradle.properties`, change `VERSION_NAME=0.1.0-SNAPSHOT` to `VERSION_NAME=0.1.0`
- [ ] Move "Unreleased" entries in `CHANGELOG.md` under a new `## [0.1.0] — 2026-MM-DD` heading
- [ ] `git commit -am "Release 0.1.0"`
- [ ] `git tag v0.1.0`
- [ ] `git push origin main --tags`
- [ ] Watch the Actions tab — `publish-release.yml` does the rest
- [ ] Once published, bump back: `VERSION_NAME=0.2.0-SNAPSHOT`, commit, push

## 5. Make the repos public

When you're ready to share:

```bash
gh repo edit ksharma-xyz/dhruva --visibility public --accept-visibility-change-consequences
gh repo edit ksharma-xyz/dhruva --visibility public --accept-visibility-change-consequences
```

The docs site at `https://ksharma-xyz.github.io/dhruva/` (and `/dhruva/`) goes live on the next push to `main` after that.

## 6. (Optional) Announce

Places that pick up new KMP libraries:

- [ ] Tweet / Bluesky / Mastodon
- [ ] Post to r/Kotlin, r/androiddev
- [ ] [KMP awesome list](https://github.com/terrakok/kmp-awesome) — open a PR
- [ ] Kotlin Slack `#multiplatform` channel
- [ ] [klibs.io](https://klibs.io/) directory

---

Reference: `docs/publishing.md` in either repo has the same flow with more detail and troubleshooting.
