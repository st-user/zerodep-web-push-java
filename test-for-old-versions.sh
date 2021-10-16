#!/bin/sh

AUTH0_VER=3.14.0
JJWT_VER=0.10.0


mvn clean test -Dauth0.version=$AUTH0_VER -Djjwt.version=$JJWT_VER -P!production-compile,jdk-8-compile
