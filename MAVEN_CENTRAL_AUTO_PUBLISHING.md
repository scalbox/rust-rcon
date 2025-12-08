# DOCUMENTATION: Automated Publishing to Maven Central with GitHub Actions

## Table of Contents

1. [Preparing GPG Keys](#1-preparing-gpg-keys)
2. [Configuring GitHub Secrets](#2-configuring-github-secrets)
3. [Updating `buildgradle`](#3-updating-buildgradle)
4. [Creating the GitHub Actions Workflow](#4-creating-the-github-actions-workflow)
5. [Optional SNAPSHOT Workflow](#5-optional-snapshot-workflow)
6. [Local Testing (Optional)](#6-local-testing-optional)
7. [Publishing a Release](#7-publishing-a-release)
8. [Monitoring the Publication](#8-monitoring-the-publication)
9. [Pre-Release Checklist](#9-pre-release-checklist)
10. [Common Issues & Troubleshooting](#10-common-issues--troubleshooting)
11. [Best Practices](#11-best-practices)
12. [Useful Resources](#12-useful-resources)
13. [Release Automation Script](#13-release-automation-script)

---

## 1. Preparing GPG Keys

### 1.1 Generate the GPG Key

```bash
# Generate a new GPG key
gpg --full-generate-key

# Choose:
# - Type: RSA and RSA
# - Key size: 4096 bits
# - Expiration: 0 (never expires) or any duration you prefer
# - Enter name, email and password
```

### 1.2 Export Key Information

```bash
# List keys to get the KEY_ID
gpg --list-secret-keys --keyid-format=long

# Example output:
# sec……rsa4096/ABCD1234EFGH5678 2024-01-01 [sc]
# The KEY_ID is: ABCD1234EFGH5678

# Export the private key in base64 format
gpg --export-secret-keys ABCD1234EFGH5678 | base64 > gpg-secret.asc

# Get the short ID (last 8 characters)
# From ABCD1234EFGH5678 -> EFGH5678
```

### 1.3 Publish the Public Key

```bash
# Publish to multiple keyservers for reliability
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678
# Or in case of error:
gpg --keyserver hkp://keyserver.ubuntu.com:80 --send-keys ABCD1234EFGH5678

gpg --keyserver keys.openpgp.org   --send-keys ABCD1234EFGH5678
gpg --keyserver pgp.mit.edu        --send-keys ABCD1234EFGH5678
```

---

## 2. Configuring GitHub Secrets

**Path:** `Repository -> Settings -> Secrets and variables -> Actions -> New repository secret`

### Required Secrets

| Secret Name       | Value                          | Description                                       |
|-------------------|--------------------------------|---------------------------------------------------|
| `OSSRH_USERNAME`  | Sonatype username              | Username of your Sonatype JIRA account           |
| `OSSRH_PASSWORD`  | Sonatype password/token        | Password or User Token from Sonatype             |
| `GPG_PRIVATE_KEY` | Content of `gpg-secret.asc`    | Private GPG key in base64                        |
| `GPG_PASSPHRASE`  | GPG key password               | Password chosen during key generation            |

### Generate Sonatype User Token (Recommended)

1. Go to <https://s01.oss.sonatype.org/>
2. Login -> **Profile** -> **User Token**
3. Click **"Access User Token"**
4. Use the generated token as `OSSRH_PASSWORD`

---

## 3. Updating `build.gradle`

### 3.1 Required Plugins

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'signing'
    id 'io.github.gradle-nexus.publish-plugin' version '2.0.0'
}
```

### 3.2 Java Configuration

```groovy
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
    withJavadocJar()
}
```

### 3.3 Publishing Configuration

```groovy
publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java

            // Artifact ID (your project name)
            artifactId = 'project-name'

            pom {
                name = 'Project Name'
                description = 'Detailed description of your project'
                url = 'https://github.com/yourusername/yourproject'
                inceptionYear = '2024'

                licenses {
                    license {
                        name = 'The Apache License, Version 2.0'
                        url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }

                developers {
                    developer {
                        id = 'yourid'
                        name = 'Your Name'
                        email = 'you@email.com'
                        organization = 'Your Organization'
                        organizationUrl = 'https://your-company.com'
                    }
                }

                scm {
                    connection = 'scm:git:git://github.com/yourusername/yourproject.git'
                    developerConnection = 'scm:git:ssh://github.com/yourusername/yourproject.git'
                    url = 'https://github.com/yourusername/yourproject'
                }

                issueManagement {
                    system = 'GitHub'
                    url = 'https://github.com/yourusername/yourproject/issues'
                }
            }
        }
    }
}
```

### 3.4 Signing Configuration

```groovy
signing {
    def signingKey = project.findProperty("gpgPrivateKey") ?: System.getenv("GPG_PRIVATE_KEY")
    def signingPassword = project.findProperty("gpgPassphrase") ?: System.getenv("GPG_PASSPHRASE")

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    useGpgCmd()
    sign publishing.publications.mavenJava
}
```

### 3.5 Nexus Publishing Configuration

```groovy
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username = project.findProperty("ossrhUsername") ?: System.getenv("OSSRH_USERNAME")
            password = project.findProperty("ossrhPassword") ?: System.getenv("OSSRH_PASSWORD")
        }
    }

    // Timeout for slow operations
    connectTimeout.set(Duration.ofMinutes(3))
    clientTimeout.set(Duration.ofMinutes(3))

    // Automatic transition after close
    transitionCheckOptions {
        maxRetries.set(50)
        delayBetween.set(Duration.ofSeconds(10))
    }
}
```

### 3.6 Verification Task (Optional)

```groovy
tasks.register('verifyPublishConfig') {
    doLast {
        println "Group: ${project.group}"
        println "Artifact: ${publishing.publications.mavenJava.artifactId}"
        println "Version: ${project.version}"
        println "OSSRH Username configured: ${project.findProperty("ossrhUsername") ?: System.getenv('OSSRH_USERNAME') != null}"
        println "OSSRH Password configured: ${project.findProperty("ossrhPassword") ?: System.getenv('OSSRH_PASSWORD') != null}"
        println "GPG Key configured: ${project.findProperty("gpgPrivateKey") ?: System.getenv('GPG_PRIVATE_KEY') != null}"
        println "GPG Passphrase configured: ${project.findProperty("gpgPassphrase") ?: System.getenv('GPG_PASSPHRASE') != null}"
    }
}
```

---

## 4. Creating the GitHub Actions Workflow

**File:** `.github/workflows/publish-maven-central.yml`

```yaml
name: Publish to Maven Central

on:
  release:
    types: [created]
  workflow_dispatch:
    inputs:
      version:
        description: ''
        required: false
        type: string

jobs:
  publish:
    runs-on: ubuntu-22.04
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'adopt'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Validate Gradle wrapper
        uses: gradle/wrapper-validation-action@v2

      - name: Decode GPG key
        run: |
          mkdir -p ~/.gnupg/
          echo "${{ secrets.GPG_PRIVATE_KEY }}" | base64 -d > ~/.gnupg/private-key.asc
          gpg --batch --import ~/.gnupg/private-key.asc
          rm ~/.gnupg/private-key.asc

      - name: Verify configuration
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
          GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
        run: ./gradlew verifyPublishConfig

      - name: Build project
        run: ./gradlew build

      - name: Publish to Maven Central
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
          GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
        run: |
          ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository \
            --no-daemon \
            --no-parallel \
            --stacktrace

      - name: Cleanup
        if: always()
        run: |
          rm -rf ~/.gnupg
          ./gradlew --stop
```

---

## 5. Optional SNAPSHOT Workflow

**File:** `.github/workflows/publish-snapshot.yml`

```yaml
name: Publish SNAPSHOT

on:
  push:
    branches:
      - develop
      - main
  workflow_dispatch:

jobs:
  publish-snapshot:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Decode GPG key
        run: |
          mkdir -p ~/.gnupg/
          echo "${{ secrets.GPG_PRIVATE_KEY }}" | base64 -d > ~/.gnupg/private-key.asc
          gpg --batch --import ~/.gnupg/private-key.asc
          rm ~/.gnupg/private-key.asc

      - name: Publish SNAPSHOT
        env:
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
          GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
          GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
        run: ./gradlew publishToSonatype --no-daemon --stacktrace

      - name: Cleanup
        if: always()
        run: |
          rm -rf ~/.gnupg
          ./gradlew --stop
```

---

## 6. Local Testing (Optional)

### 6.1 Create Local `gradle.properties`

**File:** `gradle.properties` (project root)

```properties
# LOCAL TESTING ONLY - DO NOT COMMIT!
# Add this file to .gitignore!

signing.keyId=EFGH5678
signing.password=YOUR_GPG_PASSWORD
signing.secretKeyRingFile=/home/youruser/.gnupg/secring.gpg

ossrhUsername=YOUR_SONATYPE_USERNAME
ossrhPassword=YOUR_SONATYPE_PASSWORD

gpgPassphrase=<YOUR_GPG_PASSPHRASE>
gpgPrivateKey=<YOUR_GPG_PRIVATE_KEY_FILE_PATH>
```

> **IMPORTANT:** Add `gradle.properties` to `.gitignore`!

### 6.2 Export the Key for Local Tests

```bash
gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

### 6.3 Test Commands

```bash
# Verify configuration
./gradlew verifyPublishConfig

# Full build
./gradlew clean build

# Test local publication (no remote publish)
./gradlew publishToMavenLocal

# Check artifacts in ~/.m2/repository/com/scalbox/
ls -la ~/.m2/repository/com/scalbox/project-name/
```

---

## 7. Publishing a Release

### Method 1: GitHub Release (Recommended)

1. On GitHub go to `Repository -> Releases -> Create a new release`
2. Click **"Choose a tag"**
3. Enter a new tag (e.g. `v2.0.0-alpha.2`)
4. Select the target branch (usually `main`)
5. Fill in:
    - **Release title:** `Release 2.0.0-alpha.2`
    - **Description:** Summary of changes
6. Click **"Publish release"**
7. The workflow will start automatically.

### Method 2: Manual Workflow Dispatch

1. Go to `Actions -> Publish to Maven Central`
2. Click **"Run workflow"**
3. Select the branch
4. (Optional) Enter the version
5. Click **"Run workflow"**

### Method 3: GitHub CLI

```bash
# Install GitHub CLI if needed
# https://cli.github.com/

# Create and push the tag
git tag v2.0.0-alpha.2
git push origin v2.0.0-alpha.2

# Create the release on GitHub
gh release create v2.0.0-alpha.2 \
  --title "Release 2.0.0-alpha.2" \
  --notes "## Changes

- Feature 1
- Feature 2
- Bug fix 3"
```

---

## 8. Monitoring the Publication

### 8.1 GitHub Actions

1. Go to **Actions** in your repository.
2. Click on the running workflow.
3. Monitor logs for each step.
4. Look for any errors in the logs.

### 8.2 Sonatype Nexus Repository Manager

1. Open <https://s01.oss.sonatype.org/>
2. Log in with your Sonatype credentials.
3. In the side menu, go to **Staging Repositories**.
4. Find your repository (e.g. `comscalbox-XXXX`).
5. Check:
    - **Content:** All files are present.
    - **Activity:** No errors.
    - **Status:** Should go from `closed` to `released`.

### 8.3 Maven Central Search

10–30 minutes after the release (can take up to 2 hours):

- **Maven Central Search:** <https://search.maven.org/>
- **Query:** `g:com.scalbox a:project-name`
- **MVN Repository:** <https://mvnrepository.com/>
- **Sonatype Search:** <https://central.sonatype.com/>

---

## 9. Pre-Release Checklist

### Account Prerequisites

- Sonatype JIRA account created (<https://issues.sonatype.org/>)
- JIRA ticket for your namespace (e.g. `com.scalbox`) created and approved
- Domain ownership verified (if required)

### GPG Configuration

- GPG key generated (4096 bits)
- Public key published to keyservers
- Private key exported in base64

### GitHub Configuration

- Secrets configured correctly:
    - `OSSRH_USERNAME`
    - `OSSRH_PASSWORD` (preferably a User Token)
    - `GPG_PRIVATE_KEY`
    - `GPG_PASSPHRASE`

### Project Configuration

- Required Gradle plugins added
- `build.gradle` updated with:
    - `publishing` configuration
    - `signing` configuration
    - `nexusPublishing` configuration
    - POM includes all mandatory information
- YAML workflows created under `.github/workflows/`
- `withSourcesJar()` and `withJavadocJar()` enabled

### Tests & Documentation

- Local tests pass
- Build is error-free
- Javadoc generation passes
- `README.md` updated with installation instructions
- `CHANGELOG.md` updated with the new version
- `LICENSE` file present in the repository

### Pre-Release

- Version updated in `build.gradle`
- Commits and push completed
- Git tag created (optional; can also be done during release)

---

## 10. Common Issues & Troubleshooting

### Issue 1: "No valid signature" or "PGP signature is invalid"

**Cause:** GPG key not properly published or not found.

**Solution:**

```bash
# Verify that the key is published
gpg --keyserver keyserver.ubuntu.com --recv-keys YOUR_KEY_ID
# Or in case of error:
gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys YOUR_KEY_ID

# Re-publish to multiple keyservers
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
# Or in case of error:
gpg --keyserver hkp://keyserver.ubuntu.com:80 --send-keys YOUR_KEY_ID

gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys YOUR_KEY_ID

# Wait 5–10 minutes for propagation
```

---

### Issue 2: "401 Unauthorized" with Sonatype

**Cause:** Wrong credentials or expired token.

**Solution:**

1. Verify username and password/token on Sonatype.
2. Generate a new User Token:
    - Go to <https://s01.oss.sonatype.org/>
    - `Profile -> User Token -> Access User Token`
3. Update the `OSSRH_PASSWORD` secret on GitHub.
4. Note: Use the **token**, not your JIRA password.

---

### Issue 3: "Staging repository not found"

**Cause:** Timeouts or network issues with Sonatype.

**Solution:**

```groovy
// Increase timeouts in build.gradle
nexusPublishing {
    repositories {
        sonatype {
            // ... other configuration ...
        }
    }

    connectTimeout.set(Duration.ofMinutes(5))
    clientTimeout.set(Duration.ofMinutes(5))

    transitionCheckOptions {
        maxRetries.set(100)
        delayBetween.set(Duration.ofSeconds(15))
    }
}
```

---

### Issue 4: "POM is invalid" or "Missing required metadata"

**Cause:** POM is missing mandatory fields.

**Required fields for Maven Central:**

- `name`
- `description`
- `url`
- `licenses` (at least one)
- `developers` (at least one)
- `scm` (connection, developerConnection, url)

**Check:**

```bash
./gradlew generatePomFileForMavenJavaPublication

# File location: build/publications/mavenJava/pom-default.xml
cat build/publications/mavenJava/pom-default.xml
```

---

### Issue 5: Build fails on GitHub Actions but works locally

**Solution:**

1. Add more logging:

```yaml
- name: Publish to Maven Central
  run: |
    ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository \
      --info \
      --stacktrace \
      --no-daemon
```

2. Inspect environment variables:

```yaml
- name: Debug secrets
  run: |
    echo "OSSRH_USERNAME is set: ${{ secrets.OSSRH_USERNAME != '' }}"
    echo "GPG_PRIVATE_KEY length: ${#GPG_PRIVATE_KEY}"
  env:
    GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
```

3. Ensure secrets have no leading/trailing spaces.

---

### Issue 6: "Execution failed for task ':signMavenJavaPublication'"

**Cause:** Problem with the GPG key or passphrase.

**Solution:**

```bash
# Verify that the exported key is correct
base64 -d gpg-secret.asc | gpg --import

# On macOS use:
base64 -D gpg-secret.asc | gpg --import

# Test signing manually
echo "test" | gpg --clearsign
```

---

### Issue 7: "Repository is already closed"

**Cause:** Workflow executed multiple times or repository already released.

**Solution:**

1. Go to <https://s01.oss.sonatype.org/>
2. Find your repository under **"Staging Repositories"**
3. If it is marked `closed`, you can click **"Release"** manually.
4. If you want to republish, click **"Drop"** and rerun the workflow.

---

### Issue 8: Javadoc fails with errors

**Cause:** Javadoc syntax errors or warnings treated as errors.

**Temporary solution (NOT recommended for production):**

```groovy
tasks.withType(Javadoc) {
    options.addStringOption('Xdoclint:none', '-quiet')
    options.addStringOption('encoding', 'UTF-8')
    options.addStringOption('charSet', 'UTF-8')
}
```

**Correct solution:** Fix the Javadoc comments.

---

## 11. Best Practices

### Versioning

- Use Semantic Versioning: `MAJOR.MINOR.PATCH` (<https://semver.org/>)
- **MAJOR:** Breaking changes
- **MINOR:** New backward-compatible features
- **PATCH:** Backward-compatible bug fixes

- Pre-release: `-alpha`, `-beta`, `-rc1`
- SNAPSHOT: `-SNAPSHOT` for development versions

### Branch Strategy

Example strategy:

- `main` (or `master`): Stable releases (e.g. `v1.0.0`, `v2.0.0`)
- `develop`: SNAPSHOT versions (e.g. `2.1.0-SNAPSHOT`)
- `feature/*`: New features in development
- `hotfix/*`: Urgent fixes for production

### Git Tags

```bash
# Always tag releases
git tag -a v2.0.0 -m "Release 2.0.0"
git push origin v2.0.0

# List all tags
git tag -l

# Delete a tag (if needed)
git tag -d v2.0.0
git push origin :refs/tags/v2.0.0
```

### Documentation

`README.md` should contain something like:

```markdown
## Installation

### Maven

```xml
<dependency>
    <groupId>com.scalbox</groupId>
    <artifactId>project-name</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.scalbox:project-name:2.0.0'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("com.scalbox:project-name:2.0.0")
```

## Usage

[Usage examples...]
```

`CHANGELOG.md`:

```markdown
# Changelog

## [2.0.0] - 2024-12-08

### Added
- New feature X
- Support for Y

### Changed
- Improved performance of Z

### Fixed
- Fixed bug #123

### Breaking Changes
- Removed deprecated method `oldMethod()`
```

### Security

1. Never commit secrets: use `.gitignore`

```gitignore
# .gitignore
gradle.properties
*.asc
*.gpg
.gnupg/
```

2. **Secret rotation:** Rotate passwords and tokens periodically.
3. **Minimal permissions:** Tokens should only have the permissions they need.
4. **Branch protection:** Protect `main` and `develop` branches.

### Testing

```bash
# Before each release
./gradlew clean
./gradlew build
./gradlew test
./gradlew javadoc

# Test local publication
./gradlew publishToMavenLocal

# Check artifacts
ls -la ~/.m2/repository/com/scalbox/project-name/
```

### Code Quality

- **Javadoc:** Document all public APIs.
- **Tests:** Keep test coverage high.
- **Linting:** Use Checkstyle or Spotless.
- **Static analysis:** Integrate SonarQube or similar tools.

### Release Process

1. Update `CHANGELOG.md`.
2. Update the version in `build.gradle`.
3. Commit: `git commit -m "chore: bump version to 2.0.0"`
4. Tag: `git tag -a v2.0.0 -m "Release 2.0.0"`
5. Push: `git push && git push --tags`
6. Create GitHub Release.
7. Monitor publication.
8. Verify on Maven Central.
9. Update documentation.
10. Announce the release.

---

## 12. Useful Resources

### Official Links

- Sonatype JIRA: <https://issues.sonatype.org/>
- Sonatype Nexus (new): <https://s01.oss.sonatype.org/>
- Sonatype Nexus (old): <https://oss.sonatype.org/>
- Maven Central Search: <https://search.maven.org/>
- Central Portal (beta): <https://central.sonatype.com/>

### Documentation

- Sonatype Publish Guide: <https://central.sonatype.org/publish/publish-guide/>
- Gradle Nexus Publish Plugin: <https://github.com/gradle-nexus/publish-plugin>
- Maven POM Reference: <https://maven.apache.org/pom.html>
- Semantic Versioning: <https://semver.org/>

### Tools

- GitHub CLI: <https://cli.github.com/>
- GPG Suite (macOS): <https://gpgtools.org/>
- Gpg4win (Windows): <https://www.gpg4win.org/>

---

## 13. Release Automation Script

**File:** `scripts/release.sh`

```bash
#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

VERSION="$1"

if [ -z "$VERSION" ]; then
  echo -e "${RED}Usage: ./release.sh <version>${NC}"
  echo -e "${YELLOW}Example: ./release.sh 2.0.0${NC}"
  exit 1
fi

echo -e "${GREEN}Starting release process for version $VERSION${NC}"

# 1. Check that the working directory is clean
if [ -n "$(git status -s)" ]; then
  echo -e "${RED}Working directory is not clean. Commit or stash changes first.${NC}"
  exit 1
fi

# 2. Update version in build.gradle
echo -e "${YELLOW}Updating version in build.gradle...${NC}"
sed -i.bak "s/version = '.*'/version = '$VERSION'/" build.gradle
rm build.gradle.bak

# 3. Commit
echo -e "${YELLOW}Committing version bump...${NC}"
git add build.gradle
git commit -m "chore: bump version to $VERSION"

# 4. Tag
echo -e "${YELLOW}Creating tag v$VERSION...${NC}"
git tag -a "v$VERSION" -m "Release $VERSION"

# 5. Push
echo -e "${YELLOW}Pushing to remote...${NC}"
git push origin main
git push origin "v$VERSION"

# 6. Create GitHub Release
echo -e "${YELLOW}Creating GitHub release...${NC}"
gh release create "v$VERSION" \
  --title "Release $VERSION" \
  --notes "Release $VERSION - See CHANGELOG.md for details" \
  --verify-tag

echo -e "${GREEN}Release process completed!${NC}"
echo -e "${GREEN}Monitor the GitHub Actions workflow for publishing status.${NC}"
```

### Usage

```bash
chmod +x scripts/release.sh
./scripts/release.sh 2.0.0
```
