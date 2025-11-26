#!/bin/sh

./mvnw clean test -pl ":zerodep-web-push-java-ext-jwt"
COMPILE_RESULT=$?

##
#
# Runs tests with the older version dependencies.
#
##

echo "Starts tests for the older versions."
./mvnw clean deploy

AUTH0_VER=3.17.0
FUSION_AUTH_VER=4.0.0
JJWT_VER=0.11.0
JOSE4J_VER=0.7.0
NIMBUS_JOSE_VER=9.0
VERTX3_VER=3.9.2
VERTX3_LATEST_VER=3.9.16
VERTX_VER=4.0.0

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-auth0" -Dauth0.version=${AUTH0_VER}
AUTH0_RET=$?

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-fusionauth" -Dfusionauth.version=${FUSION_AUTH_VER}
FUSION_AUTH_RET=$?

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-jjwt" -Djjwt.version=${JJWT_VER}
JJWT_RET=$?

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-jose4j" -Djose4j.version=${JOSE4J_VER}
JOSE4J_RET=$?

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-nimbus-jose" -Dnimbus.jose.version=${NIMBUS_JOSE_VER}
NIMBUS_JOSE_RET=$?

echo "Starts testing Vert.x v3"

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-vertx" -Dvertx.version=${VERTX3_VER}
VERTX3_RET=$?

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-vertx" -Dvertx.version=${VERTX3_LATEST_VER}
VERTX3_LATEST_RET=$?

echo "Ends testing Vert.x v3"

./mvnw surefire:test -pl "ext-jwt/zerodep-web-push-java-ext-jwt-vertx" -Dvertx.version=${VERTX_VER}
VERTX_RET=$?

RESULT=`expr ${COMPILE_RESULT} + ${AUTH0_RET} + ${FUSION_AUTH_RET} + ${JJWT_RET} + ${JOSE4J_RET} + ${NIMBUS_JOSE_RET} + ${VERTX3_RET} + ${VERTX3_LATEST_RET} + ${VERTX_RET}`

if [ $RESULT -ne 0 ]
then
  echo "There are test failures($RESULT)."
  echo "Return codes: ${COMPILE_RESULT},${AUTH0_RET},${FUSION_AUTH_RET},${JJWT_RET},${JOSE4J_RET},${NIMBUS_JOSE_RET},${VERTX3_RET},${VERTX3_LATEST_RET},${VERTX_RET}"
  exit 1
fi
