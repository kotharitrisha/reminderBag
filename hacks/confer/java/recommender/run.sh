#!/bin/sh
abspath=$(cd "$(dirname "$0")"; pwd)
rm -rf $abspath/bin
mkdir $abspath/bin
CLASSPATH=$CLASSPATH:$abspath/lib/*:$abspath/bin
javac -classpath $CLASSPATH -d $abspath/bin $abspath/src/edu/mit/csail/confer/ConferRecommender.java
target=$abspath/../../data/$1
export CLASSPATH
java -classpath $CLASSPATH edu.mit.csail.confer.ConferRecommender
