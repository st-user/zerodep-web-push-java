#!/bin/sh

AUTH0_VER=3.14.0
JJWT_VER=0.11.0
NIMBUS_JOSE=9.0

mvn clean test -pl zerodep-web-push-java-ext-auth0 -Dauth0.version=$AUTH0_VER -P!production-compile,jdk-8-compile
mvn clean test -pl zerodep-web-push-java-ext-nimbus-jose -Dnimbus.jose.version=$NIMBUS_JOSE
mvn clean test -pl zerodep-web-push-java-ext-jjwt -Djjwt.version=$JJWT_VER
