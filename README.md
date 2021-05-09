# Dead link finder

Find links that don't work on server-side rendered sites.

## Usage
There are two packages:
 - `dlf` - native - smaller docker image (80 MB)
 - `dlfj`- running on JVM - much faster, larger docker image (300 MB)

For now, only dockerized versions are avaiable, but you can build binaries yourself, see guide at the bottom of the file.

All examples will use `dlfj` but in any of them you can replace it with `dlf`.
```shell
# basic usage - crawl all links recursively with no limit
# and exit with success status if no broken links are found
dlfj -v 'https://json.org/example.html'
```
```shell
# using docker
docker run --rm docker.pkg.github.com/ehmeed/dead-link-finder/dlfj:latest \
  -v 'https://json.org/example.html'
```
```shell
# without -v or --verbose, only broken links will be printed
dlfj 'https://json.org/example.html'
```
```shell
# on big sites it is a good idea to set maximum recursion level using -d or --depth
# this will still run for a long time because there is too many links
dlfj -v -d 2 'https://en.wikipedia.org/wiki/Hyperlink'
```
```shell
# or ignore other domains
dlfj -v -d 2 --cross-domain dont-recurse 'https://en.wikipedia.org/wiki/Hyperlink'
```
```shell
# adding --show-text will try to display text shown on the html anchor element
dlfj -v -d 2 --cross-domain dont-recurse --show-text 'https://en.wikipedia.org/wiki/Hyperlink'
```
```shell
# to add request headers, use the -H parameter
dlfj -v -H 'authorization:  Basic BbCdefgSishGmop=' \
 -H 'User-Agent:Mozilla:4.0' 'https://en.wikipedia.org/wiki/Hyperlink'
```
```shell
# for more options see
dlfj --help
```

## Build
Compile executables:
```shell
./gradlew build
```
Native located in `./build/bin/native/debugExecutable/dead-link-finder.kexe`

JVM located in `./build/libs/dead-link-finder-jvm-{version}-SNAPSHOT.jar`

To builder docker images, use:
```shell
./scripts/install_docker.sh
```
To build and install locally, use:
```shell
./scripts/install.sh --rebuild /my/directory/bin
```
## Testing
To run all tests:
```shell
./gradlew allTests
./scripts/run_tests.sh
```
