FROM openjdk:11.0.11-jdk as builder

# RUN apt-get update && apt-get install -y libncurses5
COPY / /dead-link-finder

WORKDIR /dead-link-finder

RUN ./gradlew jvmJar
RUN ./scripts/install.sh "" .


# docker build -t docker.pkg.github.com/ehmeed/dead-link-finder/dlfj:latest .
# docker push docker.pkg.github.com/ehmeed/dead-link-finder/dlfj:latest
# docker run --rm docker.pkg.github.com/ehmeed/dead-link-finder/dlfj:latest ./dlfj --verbose --show-text -H "authorization:  Basic em9lOktkNDRSMm5hYWJkdQ==" --cross-domain ignore "https://zoe.lundegaard.ai/docs"
