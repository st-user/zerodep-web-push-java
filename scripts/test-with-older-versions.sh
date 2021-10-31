#!/bin/sh

./mvnw clean test
COMPILE_RESULT=$?

##
#
# Runs tests with the older version dependencies.
#
##
OKHTTP_VER=4.8.0
APACHE_HTTP_CLIENT_VER=5.1
JETTY_VER=10.0.0
VERTX_VER=3.9.2
VERTX3_LATEST_VER=3.9.9

echo "Starts tests for the older versions."

./mvnw surefire:test \
    -Dokhttp.version=${OKHTTP_VER} \
    -Dapache.http.client.version=${APACHE_HTTP_CLIENT_VER} \
    -Djetty.client.version=${JETTY_VER} \
    -Dvertx.version=${VERTX_VER} \
    -Dtest="com/zerodeplibs/webpush/httpclient/*"
TEST_RESULT=$?

./mvnw surefire:test \
    -Dvertx.version=${VERTX3_LATEST_VER} \
    -Dtest=VertxWebClientRequestPreparerTests
TEST_RESULT_VERTX3_LATEST=$?

RESULT=`expr ${COMPILE_RESULT} + ${TEST_RESULT} + ${TEST_RESULT_VERTX3_LATEST}`

if [ ${RESULT} -ne 0 ]
then
  echo "There are test failures(${RESULT})."
  echo "Return codes: ${COMPILE_RESULT},${TEST_RESULT},${TEST_RESULT_VERTX3_LATEST}"
  exit 1
fi
