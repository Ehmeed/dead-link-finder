#!/usr/bin/env bash
set -euo pipefail

version=${1:-latest}

push="${2:-""}"

test $(basename $(pwd)) == "dead-link-finder" || (echo "Run from root repository directory" && exit 1)

echo "Building dlf version ${version}"

docker build -f Dockerfile.build -t docker.pkg.github.com/ehmeed/dead-link-finder/dlf-builder:${version} .
docker build -f Dockerfile.jvm -t docker.pkg.github.com/ehmeed/dead-link-finder/dlfj:${version} .
docker build -f Dockerfile.native -t docker.pkg.github.com/ehmeed/dead-link-finder/dlf:${version} .

if [[ ${push} == "--push" ]]; then
  docker push docker.pkg.github.com/ehmeed/dead-link-finder/dlfj:${version}
  docker push docker.pkg.github.com/ehmeed/dead-link-finder/dlf:${version}
fi
