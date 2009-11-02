#!/bin/bash

NAME=Arity
SRCS=src/calculator/Calculator.java
LIBS=`find libs -name "*.jar"`
KEYSTORE=/home/preda/cheie/and
KEYALIAS=and

SDK=/home/preda/sdk
PLATFORM=$SDK/platforms/android-1.6/
AAPT=$PLATFORM/tools/aapt
DX=$PLATFORM/tools/dx
AJAR=$PLATFORM/android.jar
PKRES=bin/resource.ap_
PROGUARD=/home/preda/proguard/lib/proguard.jar
OUT=bin/$NAME-unalign.apk
ALIGNOUT=bin/$NAME.apk
set -e

rm -rf bin
mkdir -p bin/classes gen

echo aapt
$AAPT package -f -m -J gen -M AndroidManifest.xml -S res -A assets -I $AJAR -F $PKRES

echo javac
javac -d bin/classes -classpath bin/classes:$LIBS -sourcepath src:gen -target 1.5 -bootclasspath $AJAR $SRCS

echo proguard
java -jar $PROGUARD -injars $LIBS:bin/classes -outjar bin/obfuscated.jar -libraryjars $AJAR @proguard.txt

echo dx
$DX --dex --output=bin/classes.dex bin/obfuscated.jar 

echo apkbuilder
apkbuilder $OUT -u -z $PKRES -f bin/classes.dex

echo jarsigner
jarsigner -keystore $KEYSTORE -storepass robert $OUT $KEYALIAS 

echo zipalign
zipalign -f 4 $OUT $ALIGNOUT
