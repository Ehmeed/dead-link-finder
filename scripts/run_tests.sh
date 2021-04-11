#!/usr/bin/env bash
set -euo pipefail

host="localhost"
port=5000
url="http://www.${host}:${port}"

rebuild="${1:-""}"

red=`tput setaf 1`
green=`tput setaf 2`
reset=`tput sgr0`

test $(basename $(pwd)) == "dead-link-finder" || (echo "Run from root repository directory" && exit 1)

. ./scripts/clean.sh || true
if [[ ${rebuild} == "--rebuild" ]]; then
  ./gradlew clean build
fi

version=$(grep 'version = ' build.gradle.kts | cut -d '=' -f2 | tr -d '" ')
echo "detected version: ${version}"
jvm_exe="java -jar ./build/libs/dead-link-finder-jvm-${version}.jar"
native_exe="./build/bin/native/releaseExecutable/dead-link-finder.kexe"

(python3 scripts/server.py &> /dev/null)&
mock_server_pid=($!)
sleep 1

test_case() {
  # test_case exe target args expected
  exe=$1
  target=$2
  args=$3
  expected=$(echo -e $4)
  echo "Trying: ${target} with args: ${args}" >&2
  echo -e "Expected: ${expected}" >&2
  actual=$(${exe} ${args} ${target})
  echo "Actual: ${actual}" >&2
  if [ "${actual}" == "${expected}" ]; then
    status=true
  else
    status=false
  fi
  if [[ "${status}" = true ]]; then
     echo "${green}OK${reset}" >&2
  else
     echo "${red}Failed!${reset}" >&2
  fi
  echo "--------------------------------" >&2
  echo "${status}"
}



executables=("${jvm_exe}" "${native_exe}")
total_tests=0
failed_tests=0
for executable in "${executables[@]}"; do
  echo "--------------------------------"
  echo "Testing for ${executable}"
  echo "--------------------------------" && echo "--------------------------------"
  while read case; do
    target=$(echo $case | jq '.target' | tr -d '"')
    args=$(echo $case | jq '.args' | tr -d '"')
    expected=$(echo $case | jq '.expected' | tr -d '"')
    status=$(test_case "${executable}" "${url}${target}" "${args}" "${expected}")
    if [[ "${status}" = false ]]; then
      failed_tests=$((failed_tests + 1))
    fi
    total_tests=$((total_tests + 1))
  done < <(jq -c '.[]' './scripts/test_cases.json')
done


if [[ "${failed_tests}" != "0" ]] || [[ "${total_tests}" == "0" ]]; then
  echo "${red}${failed_tests}/${total_tests} tests failed!${reset}"
else
  echo "${green}Run ${total_tests} tests successfully!${reset}"
fi

echo "killing mock server"
. ./scripts/clean.sh
