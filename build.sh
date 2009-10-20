#!/bin/bash

NAME=Arity
SRCS=src/calculator/Calculator.java
LIBS=libs/arity-1.3.4.jar
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
mkdir -p bin/classes gen
rm -f $OUT $ALIGNOUT

echo aapt
$AAPT package -f -m -J gen -M AndroidManifest.xml -S res -I $AJAR -F $PKRES

echo javac
javac -d bin/classes -classpath bin/classes:$LIBS -sourcepath src:gen -target 1.5 -bootclasspath $AJAR -g $SRCS

echo proguard
java -jar $PROGUARD -injars $LIBS:bin/classes -outjar bin/obfuscated.jar -libraryjars $AJAR @proguard.txt

echo dx
$DX --dex --output=bin/classes.dex bin/obfuscated.jar 

echo apkbuilder
apkbuilder $OUT -u -z $PKRES -f bin/classes.dex

echo jarsigner
jarsigner -keystore $KEYSTORE $OUT $KEYALIAS

echo zipalign
zipalign -f 4 $OUT $ALIGNOUT
