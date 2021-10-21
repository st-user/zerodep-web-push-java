#!/bin/sh

##
#
# Runs tests with the older version dependencies.
#
##

AUTH0_VER=3.17.0
FUSION_AUTH_VER=4.0.0
JJWT_VER=0.11.0
JOSE4J_VER=0.7.0
NIMBUS_JOSE_VER=9.0
VERTX3_VER=3.9.2
VERTX3_LATEST_VER=3.9.9
VERTX_VER=4.0.0

# Mainly for mac OS
SED_CMD=`which gsed`
if [ "$SED_CMD" = "" ]
then
    echo "There isn't a gsed command."
    SED_CMD=`which sed`
fi

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-auth0 -Dauth0.version=$AUTH0_VER
AUTH0_RET=$?

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-fusionauth -Dfusionauth.version=$FUSION_AUTH_VER
FUSION_AUTH_RET=$?

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-jjwt -Djjwt.version=$JJWT_VER
JJWT_RET=$?

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-jose4j -Djose4j.version=$JOSE4J_VER
JOSE4J_RET=$?

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-nimbus-jose -Dnimbus.jose.version=$NIMBUS_JOSE_VER
NIMBUS_JOSE_RET=$?

echo "Starts testing Vert.x v3"
$SED_CMD -i 's/return Vertx4Support.createOptions/\/\/return Vertx4Support.createOptions/g' zerodep-web-push-java-ext-jwt-vertx/src/main/java/com/zerodeplibs/webpush/ext/jwt/vertx/VertxVAPIDJWTGenerator.java

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-vertx -Dvertx.version=$VERTX3_VER -Pvertx-3-compile
VERTX3_RET=$?

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-vertx -Dvertx.version=$VERTX3_LATEST_VER -Pvertx-3-compile
VERTX3_LATEST_RET=$?

$SED_CMD -i 's/\/\/return Vertx4Support.createOptions/return Vertx4Support.createOptions/g' zerodep-web-push-java-ext-jwt-vertx/src/main/java/com/zerodeplibs/webpush/ext/jwt/vertx/VertxVAPIDJWTGenerator.java
echo "Ends testing Vert.x v3"

./mvnw clean test -pl zerodep-web-push-java-ext-jwt-vertx -Dvertx.version=$VERTX_VER
VERTX_RET=$?

RESULT=`expr $AUTH0_RET + $FUSION_AUTH_RET + $JJWT_RET + $JOSE4J_RET + $NIMBUS_JOSE_RET + $VERTX3_RET + $VERTX3_LATEST_RET + $VERTX_RET`

if [ $RESULT -ne 0 ]
then
  echo "There are test failures($RESULT)."
  echo "Return codes: $AUTH0_RET,$FUSION_AUTH_RET,$JJWT_RET,$JOSE4J_RET,$NIMBUS_JOSE_RET,$VERTX3_RET,$VERTX3_LATEST_RET,$VERTX_RET"
  exit 1
fi
