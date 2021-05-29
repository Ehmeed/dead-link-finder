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
  # test_case exe url case
  # case
  #   - path
  #   - args
  #   - expected_visited
  #   - expected_dead
  exe=$1
  case=$3
  target="${2}"$(echo $case | jq '.path' | tr -d '"')
  args=$(echo $case | jq '.args' | tr -d '"')
  expected_visited=$(echo $case | jq '.expected_visited' | tr -d '"')
  expected_dead=$(echo $case | jq '.expected_dead' | tr -d '"')
  expected_status=$(( expected_dead == 0 ? 0 : 1 ))

  echo "Calling: ${target} with args: ${args}" >&2
  echo "Expected visited: ${expected_visited}" >&2
  echo "Expected dead: ${expected_dead}" >&2
  echo "Expected return code: ${expected_status}" >&2
  echo >&2
  actual=$(${exe} ${args} ${target})
  actual_status=$?
  echo "Actual: ${actual}" >&2
  results=$(echo $actual | sed -En -e '/No dead links/ s/.*No dead links found out of ([0-9]+) visited urls.*/\1 0/ p' -e '/Found [0-9]+ dead links out of [0-9]+ visited urls/ s/.*Found ([0-9]+) dead links out of ([0-9]+) visited urls:.*/\2 \1/ p')
  actual_visited=$(echo $results | cut -d' ' -f1)
  actual_dead=$(echo $results | cut -d' ' -f2)
  echo "Actual visited: ${actual_visited}" >&2
  echo "Actual dead: ${actual_dead}" >&2
  echo "Actual return code: ${actual_status}" >&2

  if [ "${actual_visited}" == "${expected_visited}" ] && [ "${actual_dead}" == "${expected_dead}" ] && [ "${actual_status}" == "${expected_status}" ]; then
    passed=true
  else
    passed=false
  fi
  if [[ "${passed}" = true ]]; then
     echo "${green}OK${reset}" >&2
  else
     echo "${red}Failed!${reset}" >&2
  fi
  echo "__________________________________________________________________________________________________________" >&2
  echo "${passed}"
}



executables=("${jvm_exe}" "${native_exe}")
total_tests=0
failed_tests=0
for executable in "${executables[@]}"; do
  echo "-----------------------------------------------------------------------------------------------------------"
  echo "Testing for ${executable}"
  echo "-----------------------------------------------------------------------------------------------------------"
  echo "-----------------------------------------------------------------------------------------------------------"
  while read case; do
    passed=$(test_case "${executable}" "${url}" "${case}")
    if [[ "${passed}" = false ]]; then
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
