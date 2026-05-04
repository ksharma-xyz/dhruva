# Publishing Dhruva

End-to-end flow for cutting a release to **Maven Central** via the new
**Central Portal** (the modern replacement for OSSRH/JIRA).

## One-time setup

### 1. Claim your namespace on Central Portal

Dhruva publishes under **`io.github.ksharma-xyz`** — the auto-validated GitHub-style namespace. Central Portal verifies it from your GitHub username, no DNS work needed.

1. Sign in at <https://central.sonatype.com/> (use the same GitHub account as the repo owner).
2. **Namespaces** → Add `io.github.ksharma-xyz`.
3. Central detects the matching GitHub account and verifies in seconds.
4. Once verified, you can publish any artifact under `io.github.ksharma-xyz:*`.

!!! note "Why io.github.* instead of xyz.ksharma?"
    Both work on Maven Central. We picked `io.github.ksharma-xyz` because it auto-verifies via GitHub and skips the DNS dance. If you ever want to migrate to a domain-based namespace later, you can publish under both — but it's a coordinate change for consumers, so pick early and stick with it.

### 2. Generate a signing key

Maven Central requires every artifact to be signed with PGP.

```bash
gpg --gen-key                     # follow the prompts; pick a passphrase
gpg --list-secret-keys --keyid-format=long
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
gpg --export-secret-keys --armor <KEY_ID> > signing.asc
```

Keep `signing.asc` safe, it's the secret half. The public half is what Central uses
to verify signatures.

### 3. Add secrets to GitHub Actions

In the repo's **Settings, then Secrets and variables, then Actions**, add:

| Secret | Value |
|---|---|
| `SONATYPE_USERNAME` | Central Portal user token name |
| `SONATYPE_PASSWORD` | Central Portal user token value (generate at <https://central.sonatype.com/account>) |
| `SIGNING_KEY` | Contents of `signing.asc` (full ASCII-armored block) |
| `SIGNING_KEY_PASSWORD` | The passphrase you set when generating the key |

The publish workflows in `.github/workflows/publish-*.yml` read these via the
`ORG_GRADLE_PROJECT_*` env-var convention that the
[`vanniktech/gradle-maven-publish-plugin`](https://vanniktech.github.io/gradle-maven-publish-plugin/)
expects.

### 4. Configure local publishing (optional)

For `./gradlew publishToMavenLocal` testing, add to `~/.gradle/gradle.properties`:

```properties
mavenCentralUsername=...
mavenCentralPassword=...
signingInMemoryKey=...   # contents of signing.asc, with literal \n for newlines
signingInMemoryKeyPassword=...
```

## Releasing

### 1. Bump versions

In `gradle.properties`:

```diff
- VERSION_NAME=0.1.0-SNAPSHOT
+ VERSION_NAME=0.1.0
```

Update `CHANGELOG.md`, move "Unreleased" entries under the new version heading and
date them.

### 2. Tag and push

```bash
git commit -am "Release 0.1.0"
git tag v0.1.0
git push origin main --tags
```

The `publish-release.yml` workflow fires on tags matching `v*` and runs
`./gradlew publishAndReleaseToMavenCentral`. With Central Portal, this
**automatically promotes** the staged artifacts to public, no manual "close & release"
step needed.

### 3. Bump back to SNAPSHOT for further work

```diff
- VERSION_NAME=0.1.0
+ VERSION_NAME=0.2.0-SNAPSHOT
```

```bash
git commit -am "Bump to 0.2.0-SNAPSHOT"
git push origin main
```

## Snapshots and Central Portal

Sonatype Central Portal does **not** accept SNAPSHOT versions, only releases.
Dhruva's `publish-snapshot.yml` workflow detects any `-SNAPSHOT` suffix in
`gradle.properties` and skips the publish step with a clean notice. So during
normal `0.X.Y-SNAPSHOT` development, pushes to `main` no-op cleanly on the
publish side; CI still runs.

The intended flow is: tag `v0.X.Y`, the `publish-release.yml` workflow pushes
the release through Central Portal. Use snapshots locally via
`./gradlew publishToMavenLocal` for testing only.

## Local dry run

Before tagging, confirm everything publishes locally:

```bash
./gradlew publishToMavenLocal --no-configuration-cache
ls ~/.m2/repository/io/github/ksharma-xyz/dhruva-state/0.1.0/
```

You should see `.aar`, `.pom`, `.module`, and `.asc` signature files.

## Troubleshooting

??? failure "`No staging profile in repository ID`"
    Old OSSRH error message. With Central Portal you should see different errors ,
    if you do see this, double-check that your `gradle.properties` has
    `SONATYPE_HOST=CENTRAL_PORTAL`.

??? failure "`signing required, but no signing configured`"
    `RELEASE_SIGNING_ENABLED=true` is set in `gradle.properties` but the four signing
    properties are not present. Either set them or run with
    `-PRELEASE_SIGNING_ENABLED=false` for local builds.

??? failure "Artifact not appearing on Central"
    Central Portal usually publishes within 10 minutes. The
    [staging UI](https://central.sonatype.com/publishing/deployments) tells you
    whether validation is still running.

## Versioning policy

Dhruva follows [SemVer](https://semver.org/) strictly.

- `0.x.y`, the API may change between minor versions. Document every change in CHANGELOG.
- `1.0.0`, public API freeze. Breaking changes wait for `2.0.0`.
- New permission families are `MINOR` bumps.
- Bug fixes are `PATCH` bumps.

## Yanking a release

Maven Central does **not** allow deletion of published artifacts. If you need to
withdraw a version, ship a new patch release with notes pointing to the replacement
and yank from the README's recommended version.
