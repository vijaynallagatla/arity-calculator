#!/bin/bash

SDK=/home/preda/sdk
PLATFORM=$SDK/platforms/android-1.6/
AAPT=$PLATFORM/tools/aapt
DX=$PLATFORM/tools/dx
AJAR=$PLATFORM/android.jar
PKRES=bin/res.ap_
PROGUARD=/home/preda/proguard/lib/proguard.jar

SRCS=src/calculator/Calculator.java
LIBS=libs/arity-1.3.4.jar
KEYSTORE=/home/preda/and.keystore
KEYALIAS=test

mkdir -p bin/classes gen

echo aapt 1
$AAPT package -f -m -J gen -M AndroidManifest.xml -S res -I $AJAR -F $PKRES

echo javac
#SRCS=`find . -name "*.java"`
#echo javac -d bin/classes -classpath bin/classes:$LIBS -sourcepath src:gen -target 1.5 -bootclasspath $AJAR -g $SRCS
javac -d bin/classes -classpath bin/classes:$LIBS -sourcepath src:gen -target 1.5 -bootclasspath $AJAR -g $SRCS

#proguard
java -jar $PROGUARD -injars $LIBS:bin/classes -outjar bin/obfuscated.jar -libraryjars $AJAR @proguard.txt

echo dx
$DX --dex --output=bin/classes.dex bin/obfuscated.jar 
#$LIBS

#echo aapt 2
#$AAPT package -f -M AndroidManifest.xml -S res -I $AJAR -F $PKRES

echo apkbuilder
apkbuilder bin/out.apk -u -z $PKRES -f bin/classes.dex
echo jarsigner
jarsigner -keystore $KEYSTORE -storepass robert bin/out.apk test
echo zipalign
zipalign -f 4 bin/out.apk bin/out-aligned.apk
