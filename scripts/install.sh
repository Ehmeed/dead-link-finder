#!/usr/bin/env bash
set -euo pipefail

target_location="/home/ehmeed/bin"

red=`tput setaf 1`
green=`tput setaf 2`
reset=`tput sgr0`

trap "echo '${red}Installation failed${reset}'" ERR

rebuild="${1:-""}"
test $(basename $(pwd)) == "dead-link-finder" || (echo "Run from root repository directory" && exit 1)
if [[ ${rebuild} == "--rebuild" ]]; then
  ./gradlew clean build
fi

version=$(grep 'version = ' build.gradle.kts | cut -d '=' -f2 | tr -d '" ')

exe="./build/bin/native/releaseExecutable/dead-link-finder.kexe"
target_name="dlf"

[[ -f "${exe}" ]] || (echo "File not found!" && exit 1)
cp "${exe}" "${target_location}/${target_name}" && echo "${green}Successfully installed ${target_name} ${version}${reset}"

jar="./build/libs/dead-link-finder-jvm-${version}.jar"
jar_runner_target_name="dlfj"
jar_runner="${target_location}/${jar_runner_target_name}"

[[ -f "${jar}" ]] || (echo "File not found!" && exit 1)
cp "${jar}" "${target_location}/" && \
 echo "java -jar ${jar} "'"$@"' > ${jar_runner} && \
 chmod +x ${jar_runner} && \
 echo "${green}Successfully installed ${jar_runner_target_name} ${version}${reset}"






