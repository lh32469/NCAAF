NCAAF graphical, weekly rankings application using Dropwizard, Swagger and Hystrix

Viewable at:

   http://www.gpc4j.org/ncaaf/ap/2017
   
   http://www.gpc4j.org/ncaaf/ap/2016

Swagger UI at:

   http://www.gpc4j.org/ncaaf/swagger

JAX-RS WADL at:

   http://www.gpc4j.org/ncaaf/application.wadl

How to start the DropWizard application
---

1. $ mvn clean
1. $ mvn package -DskipTests -Djavax.xml.accessExternalSchema=all
1. $ java -jar target/ncaaf-1.0-SNAPSHOT.jar server config.yml

