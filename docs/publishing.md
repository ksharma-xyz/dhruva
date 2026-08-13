# Publishing Dhruva

End-to-end flow for cutting a release to **Maven Central** via the new
**Central Portal** (the modern replacement for OSSRH/JIRA).

## One-time setup

### 1. Claim your namespace on Central Portal

Dhruva publishes under **`xyz.ksharma`**, the domain-verified namespace, as of 0.2.0.

1. Sign in at <https://central.sonatype.com/>.
2. **Namespaces** → Add `xyz.ksharma`.
3. Central issues a TXT record to add to the `ksharma.xyz` DNS zone, then verifies it.
4. Once verified, you can publish any artifact under `xyz.ksharma:*`.

!!! note "0.1.1 and earlier are at a different coordinate"
    Dhruva originally published under `io.github.ksharma-xyz`, the GitHub-style
    namespace that auto-verifies without DNS work. 0.2.0 moved to `xyz.ksharma`
    so the group ID matches the `xyz.ksharma.*` package names the library has
    always shipped.

    Maven Central coordinates are immovable, so this is a clean split rather
    than a migration: 0.1.0 and 0.1.1 remain at `io.github.ksharma-xyz`
    forever, and 0.2.0 onward is at `xyz.ksharma`. Consumers change the group
    when they upgrade; artifact names and the API are untouched.

    Aagya made the same move at its 0.3.0, so both libraries now share the
    `xyz.ksharma` namespace.

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

!!! warning "Step 3 is not optional"
    Leaving `main` on a release version means the next push to `main` is a
    non-SNAPSHOT commit, and `publish-snapshot.yml` will skip it rather than
    publish anything. Downstream apps then silently stop receiving new
    snapshots until someone notices. Bump back in the same session as the
    release.

## Pre-release channels

Two channels exist for getting unreleased changes into a consuming app. They
answer different questions.

### Snapshots — "is my fix on main yet?"

`main` stays on `x.y.z-SNAPSHOT` between releases, and every push republishes
that coordinate to the Central Portal snapshot repository. No tag, no
ceremony, nothing permanent.

!!! danger "Two prerequisites, or the publish fails"
    1. **The namespace must have snapshots enabled.** On
       <https://central.sonatype.com/publishing/namespaces>, click the three
       dots next to `xyz.ksharma` and select **Enable SNAPSHOTs**.
       This is a one-time manual step and cannot be automated. The namespace
       is shared with Aagya, so enabling it once covers both libraries.
    2. **`vanniktech-publish` must be 0.31.0 or newer.** Central Portal
       snapshot support landed in 0.31.0. On 0.30.0 the publish fails at
       execution time with `Snapshots are not supported when publishing
       through the central portal`, regardless of the namespace setting.

Consuming apps opt in with an extra repository:

```kotlin
repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation("xyz.ksharma:dhruva-data:0.2.0-SNAPSHOT")
}
```

A snapshot coordinate is **mutable** — `0.2.0-SNAPSHOT` today is not the same
build as `0.2.0-SNAPSHOT` tomorrow. Gradle caches changing modules for 24 hours
by default, so an app can quietly test stale bits and you will chase a bug that
was already fixed. Disable that caching wherever you consume snapshots:

```kotlin
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
```

### Release candidates — "are we shipping this?"

An RC is a real, immutable Maven Central release with a pre-release suffix. Cut
one when a version is feature-complete and you want a build that internal apps
can pin and reproduce.

```bash
# in gradle.properties: VERSION_NAME=0.2.0-rc.1
git commit -am "0.2.0-rc.1"
git tag v0.2.0-rc.1
git push origin main --tags
```

`publish-release.yml` handles it exactly like a release — the tag filter is
`v*` — with two differences: the GitHub Release is marked as a pre-release, so
it never displaces the current release on the repo front page, and Gradle's
version ordering already knows `0.2.0-rc.1` sorts below `0.2.0`.

Every RC is permanent and public, the same as any release. Cut them for
candidates you intend to promote, not per commit — that is what snapshots are
for. After tagging an RC, bump `main` back to `0.2.0-SNAPSHOT` so snapshot
publishing resumes.

### Which one

| | Snapshot | RC |
|---|---|---|
| Triggered by | every push to `main` | a tag |
| Mutable | yes | no |
| Permanent | no | yes, forever |
| Reproducible build | no | yes |
| Use for | continuous dogfooding | sign-off before a release |

## Local dry run

Before tagging, confirm everything publishes locally:

```bash
./gradlew publishToMavenLocal --no-configuration-cache
ls ~/.m2/repository/xyz/ksharma/dhruva-state/0.2.0/
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
