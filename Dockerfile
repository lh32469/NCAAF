from openjdk:11

COPY target/ncaaf-*.jar /usr/src/ncaaf.jar
COPY docker.yml         /usr/src
WORKDIR                 /usr/src/

EXPOSE 9020

CMD ["java", "-jar", "ncaaf.jar", "server", "docker.yml" ]

