# Manual Publishing to Maven Central via Central Portal

This guide explains how to generate artifacts locally, create a ZIP bundle with the correct folder structure, and upload it manually to **Sonatype Central Portal**.

---

## Step 1: Generate artifacts locally

In `build.gradle`, remove the `nexusPublishing` plugin and configure only local publishing:

```groovy
plugins {
    id 'java'
    id 'maven-publish'
    id 'signing'
    // REMOVE nexusPublishing for now
    // id 'io.github.gradle-nexus.publish-plugin' version '2.0.0'
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
                        id = 'yourid'
                        name = 'Your Name'
                        email = 'you@email.com'
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

    // For: Local repository
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

## Step 2: Publish locally

```bash
./gradlew clean publishToMavenLocal
```

Or:

```bash
./gradlew clean publishMavenJavaPublicationToLocalRepository
```

This creates all signed artifacts in:

```text
build/repo/com/scalbox/rust-rcon/<version>/
```

example

```text
build/repo/com/scalbox/rust-rcon/2.0.0/
```

---

## Step 3: Create the ZIP bundle with the correct structure

The ZIP **must** contain the full folder structure:

```text
com/scalbox/rust-rcon/<version>/
```

### Step 3.1: Go to the `build/repo` folder

```powershell
cd build\repo
```

### Step 3.2: Compress from there

```powershell
Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-<VERSION>-bundle.zip
```

example

```powershell
Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-2.0.0-bundle.zip
```

This way, the ZIP content will be:

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
        ... (all the other files)
```

---

## Step 4: Upload to Central Portal

1. Go to: <https://central.sonatype.com/>
2. Log in with Google SSO.
3. In the left menu, click **"Publish"**.
4. Click **"Upload Component"** (or **"Upload Bundle"**).
5. Select the file `rust-rcon-2.0.0-bundle.zip`.
6. Click **"Upload"**.
7. The system will check:
    - ✔️ All files are signed with GPG
    - ✔️ The POM is valid
    - ✔️ Javadoc and sources are present
8. If everything is OK, click **"Publish"**.
9. Publishing to Maven Central takes **a few hours** (up to 24h).

### Verify the publication

After a few hours, your artifact will be visible at:

- **Maven Central Search:**  
  <https://search.maven.org/artifact/com.scalbox/rust-rcon/2.0.0/jar>
- **Direct repository:**  
  <https://repo1.maven.org/maven2/com/scalbox/rust-rcon/2.0.0/>

### Important notes

⚠️ Before uploading, make sure that:

1. The `com.scalbox` namespace is verified on <https://central.sonatype.com/> (menu **"Namespaces"**).
2. The GPG key is **published** to a keyserver:

   ```bash
   gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
   ```

3. All files have the `.asc` signature.

---

## Full commands

From the **project root**:

```powershell
# Generate artifacts
./gradlew clean publishMavenJavaPublicationToLocalRepository

# Go to build/repo
cd build\repo

# Create the bundle with the correct structure
# Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-<VERSION>-bundle.zip -Force

# example

# Create the bundle with the correct structure
Compress-Archive -Path com -DestinationPath ..\..\rust-rcon-2.0.0-bundle.zip -Force

# Go back
cd ..\..
```

Now the file `rust-rcon-2.0.0-bundle.zip` in the project root is ready to be uploaded to **Central Portal**.
