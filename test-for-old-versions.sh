#!/bin/sh

AUTH0_VER=3.14.0
JJWT_VER=0.11.0
NIMBUS_JOSE=9.0
FUSION_AUTH_VER=4.0.0
JOSE4J_VER=0.7.0

mvn clean test -pl zerodep-web-push-java-ext-auth0 -Dauth0.version=$AUTH0_VER -P!production-compile,jdk-8-compile
mvn clean test -pl zerodep-web-push-java-ext-nimbus-jose -Dnimbus.jose.version=$NIMBUS_JOSE
mvn clean test -pl zerodep-web-push-java-ext-jjwt -Djjwt.version=$JJWT_VER
mvn clean test -pl zerodep-web-push-java-ext-fusionauth -Dfusionauth.version=$FUSION_AUTH_VER
mvn clean test -pl zerodep-web-push-java-ext-jose4j -Djose4j.version=$JOSE4J_VER

