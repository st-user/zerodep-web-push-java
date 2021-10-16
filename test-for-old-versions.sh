#!/bin/sh

AUTH0_VER=3.14.0


mvn clean test -Dauth0.version=$AUTH0_VER -P!production-compile,jdk-8-compile
