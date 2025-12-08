# DOCUMENTAZIONE: Pubblicazione Automatica su Maven Central con GitHub Actions

## Indice

1. [Preparazione delle Chiavi GPG](#1-preparazione-delle-chiavi-gpg)
2. [Configurare i Secrets su GitHub](#2-configurare-i-secrets-su-github)
3. [Aggiornare `buildgradle`](#3-aggiornare-buildgradle)
4. [Creare il Workflow GitHub Actions](#4-creare-il-workflow-github-actions)
5. [Workflow Opzionale per SNAPSHOT](#5-workflow-opzionale-per-snapshot)
6. [Testare Localmente (Opzionale)](#6-testare-localmente-opzionale)
7. [Pubblicare una Release](#7-pubblicare-una-release)
8. [Monitorare la Pubblicazione](#8-monitorare-la-pubblicazione)
9. [Checklist Pre-Pubblicazione](#9-checklist-pre-pubblicazione)
10. [Risoluzione Problemi Comuni](#10-risoluzione-problemi-comuni)
11. [Best Practices](#11-best-practices)
12. [Risorse Utili](#12-risorse-utili)
13. [Script per Automatizzare il Release](#13-script-per-automatizzare-il-release)

---

## 1. Preparazione delle Chiavi GPG

### 1.1 Generare la Chiave GPG

```bash
# Genera una nuova chiave GPG
gpg --full-generate-key

# Scegli:
# - Tipo: RSA and RSA
# - Dimensione: 4096 bit
# - Validità: 0 (non scade mai) o scegli una durata
# - Inserisci nome, email e password
```

### 1.2 Esportare le Informazioni della Chiave

```bash
# Lista le chiavi per ottenere il KEY_ID
gpg --list-secret-keys --keyid-format=long

# Output esempio:
# sec……rsa4096/ABCD1234EFGH5678 2024-01-01 [sc]
# Il KEY_ID è: ABCD1234EFGH5678

# Esporta la chiave privata in formato base64
gpg --export-secret-keys ABCD1234EFGH5678 | base64 > gpg-secret.asc

# Ottieni l'ID corto (ultimi 8 caratteri)
# Da ABCD1234EFGH5678 -> EFGH5678
```

### 1.3 Pubblicare la Chiave Pubblica

```bash
# Pubblica su più keyserver per affidabilità
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234EFGH5678
gpg --keyserver keys.openpgp.org --send-keys ABCD1234EFGH5678
gpg --keyserver pgp.mit.edu --send-keys ABCD1234EFGH5678
```

---

## 2. Configurare i Secrets su GitHub

**Percorso:** `Repository -> Settings -> Secrets and variables -> Actions -> New repository secret`

### Secrets Richiesti

| Nome Secret       | Valore                        | Descrizione                                       |
|-------------------|-------------------------------|---------------------------------------------------|
| `OSSRH_USERNAME`  | Username Sonatype             | Username del tuo account JIRA Sonatype            |
| `OSSRH_PASSWORD`  | Password/Token Sonatype       | Password o User Token da Sonatype                 |
| `GPG_PRIVATE_KEY` | Contenuto di `gpg-secret.asc` | La chiave privata GPG in base64                   |
| `GPG_PASSPHRASE`  | Password della chiave GPG     | La password scelta durante la generazione         |

### Generare User Token Sonatype (Consigliato)

1. Vai su: <https://s01.oss.sonatype.org/>
2. Login -> **Profile** -> **User Token**
3. Clicca **"Access User Token"**
4. Usa il token generato come `OSSRH_PASSWORD`

---

## 3. Aggiornare `build.gradle`

### 3.1 Plugin Richiesti

```groovy
plugins {
    id 'java-library'
    id 'maven-publish'
    id 'signing'
    id 'io.github.gradle-nexus.publish-plugin' version '2.0.0'
}
```

### 3.2 Configurazione Java

```groovy
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

    withSourcesJar()
    withJavadocJar()
}
```

### 3.3 Configurazione Publishing

```groovy
publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java

            // Artifact ID (nome del tuo progetto)
            artifactId = 'nome-progetto'

            pom {
                name = 'Nome del Progetto'
                description = 'Descrizione dettagliata del tuo progetto'
                url = 'https://github.com/tuousername/tuoprogetto'
                inceptionYear = '2024'

                licenses {
                    license {
                        name = 'The Apache License, Version 2.0'
                        url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }

                developers {
                    developer {
                        id = 'tuoid'
                        name = 'Tuo Nome'
                        email = 'tua@email.com'
                        organization = 'Tua Organizzazione'
                        organizationUrl = 'https://tuosito.com'
                    }
                }

                scm {
                    connection = 'scm:git:git://github.com/tuousername/tuoprogetto.git'
                    developerConnection = 'scm:git:ssh://github.com:tuousername/tuoprogetto.git'
                    url = 'https://github.com/tuousername/tuoprogetto'
                }

                issueManagement {
                    system = 'GitHub'
                    url = 'https://github.com/tuousername/tuoprogetto/issues'
                }
            }
        }
    }
}
```

### 3.4 Configurazione Signing

```groovy
signing {
    def signingKey = System.getenv('GPG_PRIVATE_KEY')
    def signingPassword = System.getenv('GPG_PASSPHRASE')

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }

    sign publishing.publications.mavenJava
}
```

### 3.5 Configurazione Nexus Publishing

```groovy
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri('https://s01.oss.sonatype.org/service/local/'))
            snapshotRepositoryUrl.set(uri('https://s01.oss.sonatype.org/content/repositories/snapshots/'))

            username = System.getenv('OSSRH_USERNAME')
            password = System.getenv('OSSRH_PASSWORD')
        }
    }

    // Timeout per operazioni lente
    connectTimeout.set(Duration.ofMinutes(3))
    clientTimeout.set(Duration.ofMinutes(3))

    // Transizione automatica dopo il close
    transitionCheckOptions {
        maxRetries.set(50)
        delayBetween.set(Duration.ofSeconds(10))
    }
}
```

### 3.6 Task di Verifica (Opzionale)

```groovy
tasks.register('verifyPublishConfig') {
    doLast {
        println "Group: ${project.group}"
        println "Artifact: ${publishing.publications.mavenJava.artifactId}"
        println "Version: ${project.version}"

        println "OSSRH Username configured: ${System.getenv('OSSRH_USERNAME') != null}"
        println "OSSRH Password configured: ${System.getenv('OSSRH_PASSWORD') != null}"
        println "GPG Key configured: ${System.getenv('GPG_PRIVATE_KEY') != null}"
        println "GPG Passphrase configured: ${System.getenv('GPG_PASSPHRASE') != null}"
    }
}
```

---

## 4. Creare il Workflow GitHub Actions

**File:** `.github/workflows/publish-maven-central.yml`

```yaml
name: Publish to Maven Central

on:
  release:
    types: [created]
  workflow_dispatch:
    inputs:
      version:
        description: "Version to publish (leave empty to use current)"
        required: false
        type: string

jobs:
  publish:
    runs-on: ubuntu-latest
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
          distribution: 'temurin'
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

## 5. Workflow Opzionale per SNAPSHOT

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

## 6. Testare Localmente (Opzionale)

### 6.1 Creare file `gradle.properties` locale

**File:** `gradle.properties` (nella root del progetto)

```properties
# SOLO PER TEST LOCALE - NON COMMITTARE!
# Aggiungi questo file a .gitignore!

signing.keyId=EFGH5678
signing.password=TUA_PASSWORD_GPG
signing.secretKeyRingFile=/home/tuoutente/.gnupg/secring.gpg

ossrhUsername=TUO_USERNAME_SONATYPE
ossrhPassword=TUA_PASSWORD_SONATYPE
```

> **IMPORTANTE:** Aggiungi `gradle.properties` al file `.gitignore`!

### 6.2 Esportare la chiave per test locale

```bash
gpg --export-secret-keys > ~/.gnupg/secring.gpg
```

### 6.3 Comandi di Test

```bash
# Verifica configurazione
./gradlew verifyPublishConfig

# Build completo
./gradlew clean build

# Test pubblicazione locale (senza effettivamente pubblicare)
./gradlew publishToMavenLocal

# Verifica gli artifact in ~/.m2/repository/com/scalbox/
ls -la ~/.m2/repository/com/scalbox/nome-progetto/
```

---

## 7. Pubblicare una Release

### Metodo 1: GitHub Release (Consigliato)

1. Vai su GitHub: `Repository -> Releases -> Create a new release`
2. Clicca su **"Choose a tag"**
3. Inserisci un nuovo tag (es. `v2.0.0-alpha.2`)
4. Seleziona il branch target (solitamente `main`)
5. Compila:
    - **Release title:** `Release 2.0.0-alpha.2`
    - **Description:** Descrizione delle modifiche
6. Clicca **"Publish release"**
7. Il workflow si attiverà automaticamente

### Metodo 2: Workflow Dispatch Manuale

1. Vai su: `Actions -> Publish to Maven Central`
2. Clicca su **"Run workflow"**
3. Seleziona il branch
4. (Opzionale) Inserisci la versione
5. Clicca **"Run workflow"**

### Metodo 3: Da Linea di Comando con GitHub CLI

```bash
# Installa GitHub CLI se non presente
# https://cli.github.com/

# Crea e pusha il tag
git tag v2.0.0-alpha.2
git push origin v2.0.0-alpha.2

# Crea la release su GitHub
gh release create v2.0.0-alpha.2 \
  --title "Release 2.0.0-alpha.2" \
  --notes "## Modifiche

- Feature 1
- Feature 2
- Bug fix 3"
```

---

## 8. Monitorare la Pubblicazione

### 8.1 GitHub Actions

1. Vai su **Actions** nel tuo repository.
2. Clicca sul workflow in esecuzione.
3. Monitora i log di ogni step.
4. Cerca eventuali errori nei log.

### 8.2 Sonatype Nexus Repository Manager

1. Accedi a: <https://s01.oss.sonatype.org/>
2. Login con le tue credenziali Sonatype.
3. Menu laterale: **Staging Repositories**.
4. Cerca il tuo repository (es. `comscalbox-XXXX`).
5. Verifica:
    - **Content:** Controlla che tutti i file siano presenti.
    - **Activity:** Verifica che non ci siano errori.
    - **Status:** Dovrebbe essere prima `closed` e poi `released`.

### 8.3 Maven Central Search

Dopo 10–30 minuti dalla release (può richiedere fino a 2 ore):

- **Maven Central Search:** <https://search.maven.org/>
- **Cerca:** `g:com.scalbox a:nome-progetto`
- **MVN Repository:** <https://mvnrepository.com/>
- **Sonatype Search:** <https://central.sonatype.com/>

---

## 9. Checklist Pre-Pubblicazione

### Prerequisiti Account

- Account Sonatype JIRA creato (<https://issues.sonatype.org/>)
- Ticket JIRA per namespace (`com.scalbox`) creato e approvato
- Proprietà del dominio verificata (se richiesto)

### Configurazione GPG

- Chiave GPG generata (4096 bit)
- Chiave pubblica pubblicata su keyserver
- Chiave privata esportata in base64

### Configurazione GitHub

- Secrets configurati correttamente:
    - `OSSRH_USERNAME`
    - `OSSRH_PASSWORD` (preferibilmente User Token)
    - `GPG_PRIVATE_KEY`
    - `GPG_PASSPHRASE`

### Configurazione Progetto

- Plugin Gradle aggiunti
- `build.gradle` aggiornato con:
    - Configurazione `publishing`
    - Configurazione `signing`
    - Configurazione `nexusPublishing`
    - POM completo con tutte le informazioni obbligatorie
- Workflow YAML creati in `.github/workflows/`
- `withSourcesJar()` e `withJavadocJar()` abilitati

### Test e Documentazione

- Test locale superato con successo
- Build senza errori
- Javadoc genera senza errori
- `README.md` aggiornato con istruzioni di installazione
- `CHANGELOG.md` aggiornato con la nuova versione
- File `LICENSE` presente nel repository

### Pre-Release

- Versione in `build.gradle` aggiornata
- Commit e push completati
- Tag Git creato (opzionale, può essere fatto durante la release)

---

## 10. Risoluzione Problemi Comuni

### Problema 1: "No valid signature" o "PGP signature is invalid"

**Causa:** La chiave GPG non è stata pubblicata correttamente o non è trovabile.

**Soluzione:**

```bash
# Verifica che la chiave sia stata pubblicata
gpg --keyserver keyserver.ubuntu.com --recv-keys TUO_KEY_ID

# Ri-pubblica su più keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys TUO_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys TUO_KEY_ID
gpg --keyserver pgp.mit.edu --send-keys TUO_KEY_ID

# Attendi 5-10 minuti per la propagazione
```

---

### Problema 2: "401 Unauthorized" con Sonatype

**Causa:** Credenziali errate o token scaduto.

**Soluzione:**

1. Verifica username e password/token su Sonatype.
2. Genera un nuovo User Token:
    - Vai su: <https://s01.oss.sonatype.org/>
    - `Profile -> User Token -> Access User Token`
3. Aggiorna il secret `OSSRH_PASSWORD` su GitHub.
4. Nota: Usa il token, **NON** la password JIRA.

---

### Problema 3: "Staging repository not found"

**Causa:** Timeout o problemi di rete con Sonatype.

**Soluzione:**

```groovy
// Aumenta i timeout in build.gradle
nexusPublishing {
    repositories {
        sonatype {
            // ... altre configurazioni ...
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

### Problema 4: "POM is invalid" o "Missing required metadata"

**Causa:** Il POM non ha tutti i campi obbligatori.

**Campi obbligatori per Maven Central:**

- `name`
- `description`
- `url`
- `licenses` (almeno una)
- `developers` (almeno uno)
- `scm` (connection, developerConnection, url)

**Verifica:**

```bash
./gradlew generatePomFileForMavenJavaPublication

# Il file sarà in: build/publications/mavenJava/pom-default.xml
cat build/publications/mavenJava/pom-default.xml
```

---

### Problema 5: Build fallisce su GitHub Actions ma funziona localmente

**Soluzione:**

1. Aggiungi più logging:

```yaml
- name: Publish to Maven Central
  run: |
    ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository \
      --info \
      --stacktrace \
      --no-daemon
```

2. Verifica le variabili d'ambiente:

```yaml
- name: Debug secrets
  run: |
    echo "OSSRH_USERNAME is set: ${{ secrets.OSSRH_USERNAME != '' }}"
    echo "GPG_PRIVATE_KEY length: ${#GPG_PRIVATE_KEY}"
  env:
    GPG_PRIVATE_KEY: ${{ secrets.GPG_PRIVATE_KEY }}
```

3. Verifica che i secrets non abbiano spazi iniziali/finali.

---

### Problema 6: "Execution failed for task ':signMavenJavaPublication'"

**Causa:** Problema con la chiave GPG o la passphrase.

**Soluzione:**

```bash
# Verifica che la chiave esportata sia corretta
base64 -d gpg-secret.asc | gpg --import

# Se usi macOS
base64 -D gpg-secret.asc | gpg --import

# Testa la firma manualmente
echo "test" | gpg --clearsign
```

---

### Problema 7: "Repository is already closed"

**Causa:** Il workflow è stato eseguito più volte o il repository è già stato rilasciato.

**Soluzione:**

1. Vai su <https://s01.oss.sonatype.org/>
2. Trova il repository in **"Staging Repositories"**
3. Se è segnato come `closed`, puoi fare **"Release"** manualmente
4. Se vuoi ripubblicare, fai **"Drop"** e rilancia il workflow

---

### Problema 8: Javadoc fallisce con errori

**Causa:** Errori nella sintassi Javadoc o warning trattati come errori.

**Soluzione temporanea (NON consigliata per produzione):**

```groovy
tasks.withType(Javadoc) {
    options.addStringOption('Xdoclint:none', '-quiet')
    options.addStringOption('encoding', 'UTF-8')
    options.addStringOption('charSet', 'UTF-8')
}
```

**Soluzione corretta:** Fixare i commenti Javadoc.

---

## 11. Best Practices

### Versioning

- Usa Semantic Versioning: `MAJOR.MINOR.PATCH` (<https://semver.org/>)
- **MAJOR:** Breaking changes
- **MINOR:** Nuove feature backward-compatible
- **PATCH:** Bug fix backward-compatible

- Pre-release: `-alpha`, `-beta`, `-rc1`
- SNAPSHOT: `-SNAPSHOT` per versioni di sviluppo

### Branch Strategy

Esempio di strategia:

- `main` (o `master`): Release stabili (es. `v1.0.0`, `v2.0.0`)
- `develop`: Versioni SNAPSHOT (es. `2.1.0-SNAPSHOT`)
- `feature/*`: Nuove feature in sviluppo
- `hotfix/*`: Fix urgenti per production

### Git Tags

```bash
# Sempre taggare le release
git tag -a v2.0.0 -m "Release 2.0.0"
git push origin v2.0.0

# Lista tutti i tag
git tag -l

# Elimina un tag (se necessario)
git tag -d v2.0.0
git push origin :refs/tags/v2.0.0
```

### Documentation

`README.md` dovrebbe contenere ad esempio:

```markdown
## Installation

### Maven

```xml
<dependency>
    <groupId>com.scalbox</groupId>
    <artifactId>nome-progetto</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.scalbox:nome-progetto:2.0.0'
```

### Gradle (Kotlin DSL)

```kotlin
implementation("com.scalbox:nome-progetto:2.0.0")
```

## Usage

[Esempi di utilizzo...]
```

`CHANGELOG.md`:

```markdown
# Changelog

## [2.0.0] - 2024-12-08

### Added
- Nuova feature X
- Supporto per Y

### Changed
- Migliorate performance di Z

### Fixed
- Risolto bug #123

### Breaking Changes
- Rimosso metodo deprecato `oldMethod()`
```

### Security

1. Mai committare secrets: usa `.gitignore`

```gitignore
# .gitignore
gradle.properties
*.asc
*.gpg
.gnupg/
```

2. **Rotazione secrets:** Cambia periodicamente password e token.
3. **Minimal permissions:** I token dovrebbero avere solo i permessi necessari.
4. **Branch protection:** Proteggi i branch `main` e `develop`.

### Testing

```bash
# Prima di ogni release
./gradlew clean
./gradlew build
./gradlew test
./gradlew javadoc

# Test pubblicazione locale
./gradlew publishToMavenLocal

# Verifica gli artifact
ls -la ~/.m2/repository/com/scalbox/nome-progetto/
```

### Code Quality

- **Javadoc:** Documenta tutte le API pubbliche.
- **Tests:** Mantieni alta la coverage.
- **Linting:** Usa Checkstyle o Spotless.
- **Static analysis:** Integra SonarQube o strumenti simili.

### Release Process

1. Aggiorna `CHANGELOG.md`.
2. Aggiorna versione in `build.gradle`.
3. Commit: `git commit -m "chore: bump version to 2.0.0"`
4. Tag: `git tag -a v2.0.0 -m "Release 2.0.0"`
5. Push: `git push && git push --tags`
6. Crea GitHub Release.
7. Monitora la pubblicazione.
8. Verifica su Maven Central.
9. Aggiorna documentazione.
10. Annuncia la release.

---

## 12. Risorse Utili

### Link Ufficiali

- Sonatype JIRA: <https://issues.sonatype.org/>
- Sonatype Nexus (nuovo): <https://s01.oss.sonatype.org/>
- Sonatype Nexus (vecchio): <https://oss.sonatype.org/>
- Maven Central Search: <https://search.maven.org/>
- Central Portal (beta): <https://central.sonatype.com/>

### Documentazione

- Sonatype Guide: <https://central.sonatype.org/publish/publish-guide/>
- Gradle Nexus Publish Plugin: <https://github.com/gradle-nexus/publish-plugin>
- Maven POM Reference: <https://maven.apache.org/pom.html>
- Semantic Versioning: <https://semver.org/>

### Tools

- GitHub CLI: <https://cli.github.com/>
- GPG Suite (macOS): <https://gpgtools.org/>
- Gpg4win (Windows): <https://www.gpg4win.org/>

---

## 13. Script per Automatizzare il Release

**File:** `scripts/release.sh`

```bash
#!/bin/bash

set -e

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

VERSION=$1

if [ -z "$VERSION" ]; then
  echo -e "${RED}Usage: ./release.sh <version>${NC}"
  echo -e "${YELLOW}Example: ./release.sh 2.0.0${NC}"
  exit 1
fi

echo -e "${GREEN}Starting release process for version $VERSION${NC}"

# 1. Verifica che la working directory sia pulita
if [ -n "$(git status -s)" ]; then
  echo -e "${RED}Working directory is not clean. Commit or stash changes first.${NC}"
  exit 1
fi

# 2. Aggiorna la versione in build.gradle
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

# 6. Crea GitHub Release
echo -e "${YELLOW}Creating Github release...${NC}"
gh release create "v$VERSION" \
  --title "Release $VERSION" \
  --notes "Release $VERSION - See CHANGELOG.md for details" \
  --verify-tag

echo -e "${GREEN}Release process completed!${NC}"
echo -e "${GREEN}Monitor the Github Actions workflow for publishing status.${NC}"
```

### Uso

```bash
chmod +x scripts/release.sh
./scripts/release.sh 2.0.0
```
