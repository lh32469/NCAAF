FROM openjdk:11

COPY target/ncaaf-*.jar /usr/src/ncaaf.jar
COPY docker.yml         /usr/src
COPY updateConsul.sh    /usr/src
COPY runApp.sh          /usr/src
WORKDIR                 /usr/src/

COPY S01consulRegistration /etc/rc3.d

EXPOSE 9020

CMD ./updateConsul.sh;./runApp.sh

