# Dead link finder

Find links that don't work on server-side rendered sites.

## Usage
```shell
# basic usage
dlfj --verbose 'https://json.org/example.html'
# or using docker
docker run --rm docker.pkg.github.com/ehmeed/dead-link-finder/dlfj:latest --verbose 'https://json.org/example.html'
docker run --rm docker.pkg.github.com/ehmeed/dead-link-finder/dlf:latest --verbose 'https://json.org/example.html'
# for more options see
dlfj --help
```

## Build
Compile executables:
```bash
./gradlew build
```
Native located in `./build/bin/native/debugExecutable/dead-link-finder.kexe`

JVM located in `./build/libs/dead-link-finder-jvm-{version}-SNAPSHOT.jar`
## Testing
To run all tests:
```bash
./gradlew allTests
```
