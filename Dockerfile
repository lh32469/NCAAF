FROM openjdk:11

COPY target/ncaaf-*.jar /usr/src/ncaaf.jar
COPY docker.yml         /usr/src
COPY runApp.sh          /usr/src
WORKDIR                 /usr/src/

# EXPOSE 9020

# Used exec fuctionality to run as PID 1 for Signal handling
CMD ["./runApp.sh"]

