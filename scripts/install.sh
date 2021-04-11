#!/usr/bin/env bash
set -euo pipefail

green=`tput setaf 2`
reset=`tput sgr0`

rebuild="${1:-""}"
test $(basename $(pwd)) == "dead-link-finder" || (echo "Run from root repository directory" && exit 1)
if [[ ${rebuild} == "--rebuild" ]]; then
  ./gradlew clean build
fi

exe="./build/bin/native/releaseExecutable/dead-link-finder.kexe"
target_name="dlf"

[[ -f "${exe}" ]] || (echo "File not found!" && exit 1)
version=$(grep 'version = ' build.gradle.kts | cut -d '=' -f2 | tr -d '" ')
cp "${exe}" "/home/ehmeed/bin/${target_name}" && echo "${green}Successfully installed ${target_name} ${version}${reset}"


