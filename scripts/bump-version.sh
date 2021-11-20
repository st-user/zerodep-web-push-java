#!/bin/sh

echo "Input the version for the next release."

read VERSION

# Mainly for mac OS
SED_CMD=`which gsed`
if [ "$SED_CMD" = "" ]
then
    echo "There isn't a gsed command."
    SED_CMD=`which sed`
fi

$SED_CMD -i "/<artifactId>zerodep-web-push-java<\/artifactId>/{n;s/<version>[0-9\\.]*<\/version>/<version>${VERSION}<\/version>/g;}" pom.xml
$SED_CMD -i "/<artifactId>zerodep-web-push-java.*<\/artifactId>/{n;s/<version>[0-9\\.]*<\/version>/<version>${VERSION}<\/version>/g;}" ./**/README.md

