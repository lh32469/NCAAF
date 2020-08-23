FROM openjdk:11

COPY target/ncaaf-*.jar /usr/src/ncaaf.jar
COPY docker.yml         /usr/src
COPY updateConsul.sh    /usr/src
COPY runApp.sh          /usr/src
WORKDIR                 /usr/src/

COPY S01consulRegistration /etc/rc3.d
COPY S01consulRegistration /etc/rcS.d

COPY S01consulRegistration /etc/rc0.d/K01consulRegistration
COPY S01consulRegistration /etc/rc6.d/K01consulRegistration

EXPOSE 9020

CMD ./runApp.sh

