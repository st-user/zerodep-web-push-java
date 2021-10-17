#!/bin/sh

AUTH0_VER=3.17.0
FUSION_AUTH_VER=4.0.0
JJWT_VER=0.11.0
JOSE4J_VER=0.7.0
NIMBUS_JOSE_VER=9.0
VERTX3_VER=3.9.2
VERTX3_LATEST_VER=3.9.9
VERTX_VER=4.0.0

mvn clean test -pl zerodep-web-push-java-ext-jwt-auth0 -Dauth0.version=$AUTH0_VER
mvn clean test -pl zerodep-web-push-java-ext-jwt-fusionauth -Dfusionauth.version=$FUSION_AUTH_VER
mvn clean test -pl zerodep-web-push-java-ext-jwt-jjwt -Djjwt.version=$JJWT_VER
mvn clean test -pl zerodep-web-push-java-ext-jwt-jose4j -Djose4j.version=$JOSE4J_VER
mvn clean test -pl zerodep-web-push-java-ext-jwt-nimbus-jose -Dnimbus.jose.version=$NIMBUS_JOSE_VER

echo "Starts testing Vert.x v3"
gsed -i 's/return Vertx4Support.createOptions/\/\/return Vertx4Support.createOptions/g' zerodep-web-push-java-ext-jwt-vertx/src/main/java/com/zerodeplibs/webpush/ext/jwt/vertx/VertxVAPIDJWTGenerator.java
mvn clean test -pl zerodep-web-push-java-ext-jwt-vertx -Dvertx.version=$VERTX3_VER -Pvertx-3-compile
mvn clean test -pl zerodep-web-push-java-ext-jwt-vertx -Dvertx.version=$VERTX3_LATEST_VER -Pvertx-3-compile
gsed -i 's/\/\/return Vertx4Support.createOptions/return Vertx4Support.createOptions/g' zerodep-web-push-java-ext-jwt-vertx/src/main/java/com/zerodeplibs/webpush/ext/jwt/vertx/VertxVAPIDJWTGenerator.java
echo "Ends testing Vert.x v3"

mvn clean test -pl zerodep-web-push-java-ext-jwt-vertx -Dvertx.version=$VERTX_VER
