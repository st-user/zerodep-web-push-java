#!/bin/sh

./mvnw clean test -pl ":zerodep-web-push-java"
COMPILE_RESULT=$?

##
#
# Runs tests with the older version dependencies.
#
##
OKHTTP_VER=4.9.0
APACHE_HTTP_CLIENT_VER=5.1
JETTY_VER=9.4.33.v20201020
JETTY10_VER=10.0.0
JETTY10_LATEST_VER=10.0.21
JETTY11_VER=11.0.0
JETTY11_LATEST_VER=11.0.21
VERTX_VER=4.0.0
VERTX3_VER=3.9.2
VERTX3_LATEST_VER=3.9.16

echo "Starts tests for the older versions."

./mvnw surefire:test -pl ":zerodep-web-push-java" \
    -Dokhttp.version=${OKHTTP_VER} \
    -Dapache.http.client.version=${APACHE_HTTP_CLIENT_VER} \
    -Djetty.client.version=${JETTY_VER} \
    -Dvertx.version=${VERTX_VER} \
    -Dtest="com/zerodeplibs/webpush/httpclient/*"
TEST_RESULT=$?

./mvnw surefire:test -pl ":zerodep-web-push-java" \
    -Djetty.client.version=${JETTY10_VER} \
    -Dtest=JettyHttpClientRequestPreparerTests
TEST_RESULT_JETTY10=$?

./mvnw surefire:test -pl ":zerodep-web-push-java" \
    -Djetty.client.version=${JETTY10_LATEST_VER} \
    -Dtest=JettyHttpClientRequestPreparerTests
TEST_RESULT_JETTY10_LATEST=$?

./mvnw surefire:test -pl ":zerodep-web-push-java" \
    -Djetty.client.version=${JETTY11_VER} \
    -Dtest=JettyHttpClientRequestPreparerTests
TEST_RESULT_JETTY11=$?

./mvnw surefire:test -pl ":zerodep-web-push-java" \
    -Djetty.client.version=${JETTY11_LATEST_VER} \
    -Dtest=JettyHttpClientRequestPreparerTests
TEST_RESULT_JETTY11_LATEST=$?

./mvnw surefire:test -pl ":zerodep-web-push-java" \
    -Dvertx.version=${VERTX3_VER} \
    -Dtest=VertxWebClientRequestPreparerTests
TEST_RESULT_VERTX3=$?

./mvnw surefire:test -pl ":zerodep-web-push-java" \
    -Dvertx.version=${VERTX3_LATEST_VER} \
    -Dtest=VertxWebClientRequestPreparerTests
TEST_RESULT_VERTX3_LATEST=$?

RESULT=`expr ${COMPILE_RESULT} + ${TEST_RESULT} + ${TEST_RESULT_JETTY10} + ${TEST_RESULT_JETTY10_LATEST} + ${TEST_RESULT_JETTY11} + ${TEST_RESULT_JETTY11_LATEST} + ${TEST_RESULT_VERTX3} + ${TEST_RESULT_VERTX3_LATEST}`

if [ ${RESULT} -ne 0 ]
then
  echo "There are test failures(${RESULT})."
  echo "Return codes: ${COMPILE_RESULT},${TEST_RESULT},${TEST_RESULT_JETTY10},${TEST_RESULT_JETTY10_LATEST},${TEST_RESULT_JETTY11},${TEST_RESULT_JETTY11_LATEST},${TEST_RESULT_VERTX3},${TEST_RESULT_VERTX3_LATEST}"
  exit 1
fi
