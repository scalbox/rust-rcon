# Pubblicazione Manuale su Maven Central tramite Central Portal

Questa guida spiega come generare gli artefatti localmente, creare un bundle ZIP con la struttura corretta e caricarlo manualmente su **Sonatype Central Portal**.

---

## Passo 1: Genera gli artefatti localmente

Nel `build.gradle`, rimuovi il plugin `nexusPublishing` e configura solo la pubblicazione locale:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
    id 'signing'
    // RIMUOVI nexusPublishing per ora
}

// ... existing code ...

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java

            pom {
                name = 'rust-rcon'
                description = 'Rust RCON client library for Java'
                url = 'https://github.com/scalbox/rust-rcon'

                licenses {
                    license {
                        name = 'The Apache License, Version 2.0'
                        url  = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                    }
                }

                developers {
                    developer {
                        id = 'tuoid'
                        name = 'Tuo Nome'
                        email = 'tua@email.com'
                    }
                }

                scm {
                    connection          = 'scm:git:git://github.com/scalbox/rust-rcon.git'
                    developerConnection = 'scm:git:ssh://github.com/scalbox/rust-rcon.git'
                    url                 = 'https://github.com/scalbox/rust-rcon'
                }
            }
        }
    }

    // Add
    repositories {
        maven {
            name = "Local"
            url  = uri("${buildDir}/repo")
        }
    }
}

signing {
    useGpgCmd()
    sign publishing.publications.mavenJavaPublication
}
```

---

## Passo 2: Pubblica localmente

```bash
./gradlew clean publishToMavenLocal
```

Oppure:

```bash
./gradlew clean publishMavenJavaPublicationToLocalRepository
```

Questo crea tutti gli artefatti firmati in:

```text
build/repo/com/scalbox/rust-rcon/<version>/
```

esempio

```text
build/repo/com/scalbox/rust-rcon/2.0.0/
```

---

## Passo 3: Crea il bundle ZIP con la struttura corretta

Lo ZIP deve contenere la **struttura di cartelle completa**:

```text
com/scalbox/rust-rcon/<version>/
```

### Passo 3.1: Vai nella cartella `build/repo`

```powershell
cd build\repo
```

### Passo 3.2: Comprimi partendo da lì

```powershell
Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-<VERSION>-bundle.zip
```

esempio

```powershell
Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-2.0.0-bundle.zip
```

In questo modo il contenuto dello ZIP sarà:

```text
com/
  scalbox/
    rust-rcon/
      2.0.0/
        rust-rcon-2.0.0.jar
        rust-rcon-2.0.0.jar.asc
        rust-rcon-2.0.0.pom
        rust-rcon-2.0.0.pom.asc
        rust-rcon-2.0.0-javadoc.jar
        rust-rcon-2.0.0-javadoc.jar.asc
        rust-rcon-2.0.0-sources.jar
        rust-rcon-2.0.0-sources.jar.asc
        ... (tutti gli altri file)
```

---

## Passo 4: Carica su Central Portal

1. Vai su: <https://central.sonatype.com/>
2. Fai login con Google SSO.
3. Nel menu a sinistra, clicca su **"Publish"**.
4. Clicca su **"Upload Component"** (o **"Upload Bundle"**).
5. Seleziona il file `rust-rcon-2.0.0-bundle.zip`.
6. Clicca su **"Upload"**.
7. Il sistema verificherà:
    - ✔️ Che tutti i file siano firmati con GPG
    - ✔️ Che il POM sia valido
    - ✔️ Che ci siano javadoc e sources
8. Se tutto è OK, clicca su **"Publish"**.
9. La pubblicazione su Maven Central richiede **qualche ora** (fino a 24h).

### Verifica la pubblicazione

Dopo qualche ora, il tuo artefatto sarà visibile su:

- **Maven Central Search:**  
  <https://search.maven.org/artifact/com.scalbox/rust-rcon/2.0.0/jar>
- **Repository diretto:**  
  <https://repo1.maven.org/maven2/com/scalbox/rust-rcon/2.0.0/>

### Note importanti

⚠️ Prima di caricare, assicurati che:

1. Il namespace `com.scalbox` sia verificato su <https://central.sonatype.com/> (menu **"Namespaces"**).
2. La chiave GPG sia **pubblicata** su un keyserver:

   ```bash
   gpg --keyserver keyserver.ubuntu.com --send-keys 0BEB6534E91C3C1F2883055105655D26854D9CAF
   ```

3. Tutti i file abbiano la firma `.asc`.

---

## Comandi completi

Dalla **root del progetto**:

```powershell
# Genera gli artefatti
./gradlew clean publishMavenJavaPublicationToLocalRepository

# Vai in build/repo
cd build\repo

# Crea il bundle con la struttura corretta
Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-<VERSION>-bundle.zip -Force

esempio

# Crea il bundle con la struttura corretta
Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-2.0.0-bundle.zip -Force

# Torna indietro
cd ..\..
```

Ora il file `rust-rcon-2.0.0-bundle.zip` nella root del progetto è pronto per essere caricato su **Central Portal**.
