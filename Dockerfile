FROM openjdk:11

COPY target/ncaaf-*.jar /usr/src/ncaaf.jar
COPY docker.yml         /usr/src
COPY updateConsul.sh    /usr/src
COPY runApp.sh          /usr/src
WORKDIR                 /usr/src/

EXPOSE 9020

ENTRYPOINT ./runApp.sh

