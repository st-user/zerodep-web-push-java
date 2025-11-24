#!/bin/sh

#
# Upgrade Maven wrapper version
#

MAVEN_VERSION=3.9.11

 ./mvnw -N wrapper:wrapper -Dmaven=${MAVEN_VERSION}
 cd ./examples/basic && ./mvnw -N wrapper:wrapper -Dmaven=${MAVEN_VERSION} && cd ../../
 cd ./examples/vertx && ./mvnw -N wrapper:wrapper -Dmaven=${MAVEN_VERSION} && cd ../../
 cd ./examples/webflux && ./mvnw -N wrapper:wrapper -Dmaven=${MAVEN_VERSION} && cd ../../
