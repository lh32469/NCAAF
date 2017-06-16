NCAAF graphical, weekly rankings application using Dropwizard, Swagger and Hystrix

Viewable at:

   http://www.gpc4j.org/ncaaf/ap/2016

Swagger UI at:

   http://www.gpc4j.org/ncaaf/swagger

JAX-RS WADL at:

   http://www.gpc4j.org/ncaaf/application.wadl

How to start the DropWizard application
---

$ mvn clean
$ mvn package -DskipTests -Djavax.xml.accessExternalSchema=all
$ java -jar target/ncaaf-1.0-SNAPSHOT.jar server config.yml

