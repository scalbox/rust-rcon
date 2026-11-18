#!/usr/bin/env bash
# Sets the library version shown in README.md (Maven and Gradle snippets)
# and the default version in build.gradle.
# Usage: set-version.sh <version|tag>   e.g. set-version.sh v2.0.0-alpha.8
set -euo pipefail

export V="${1#v}"
if [[ ! "$V" =~ ^[0-9][0-9A-Za-z.+-]*$ ]]; then
    echo "Invalid version: '$1'" >&2
    exit 1
fi

# README.md: <artifactId>rust-rcon</artifactId> ... <version>X</version>
perl -0pi -e 's#(<artifactId>rust-rcon</artifactId>\s*<version>)[^<]+(</version>)#${1}$ENV{V}${2}#g' README.md
# README.md: implementation("com.scalbox:rust-rcon:X")
perl -pi -e 's#(com\.scalbox:rust-rcon:)[0-9][0-9A-Za-z.+-]*#${1}$ENV{V}#g' README.md
# build.gradle: version = releaseVersion ?: 'X'
perl -pi -e "s#^(version = releaseVersion \?: ')[^']+(')#\${1}\$ENV{V}\${2}#" build.gradle

# Fail if a pattern no longer matches (e.g. the README was restructured)
grep -q "<version>$V</version>" README.md || { echo "README.md: Maven version not updated" >&2; exit 1; }
grep -q "com.scalbox:rust-rcon:$V\"" README.md || { echo "README.md: Gradle version not updated" >&2; exit 1; }
grep -q "releaseVersion ?: '$V'" build.gradle || { echo "build.gradle: version not updated" >&2; exit 1; }
echo "Version set to $V"
