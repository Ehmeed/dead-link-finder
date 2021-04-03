#!/usr/bin/env bash
set -feuo pipefail

./gradlew build

version=$(grep 'version = ' build.gradle.kts | cut -d '=' -f2 | tr -d '" ')
jvm_exe="./build/libs/dead-link-finder-jvm-{version}-SNAPSHOT.jar"
native_exe="./build/bin/native/debugExecutable/dead-link-finder.kexe"

python3 server.py
... todo


# TODO
# mutation of frozen logger when running native exe
# no main manifest when running jar
