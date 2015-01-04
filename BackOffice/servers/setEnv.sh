#!/bin/bash
##########################################
# Set environment variables
##########################################

export GEMFIRE_HOME=/Users/wmarkito/Pivotal/GemFire/installers/gemfire-latest
export CONF_DIR=./conf
export JAVA_HOME=`/usr/libexec/java_home -v1.7`
export PATH=$GEMFIRE_HOME/bin:$PATH
export SERVER_HEAP=4G

export GF_JAVA_ARGS="--J-verbose:gc --J-XX:+PrintGCTimeStamps --J-XX:+PrintGCDetails --J-Xloggc:gc.log \
--J-XX:+UseConcMarkSweepGC --J-XX:+UseParNewGC \
--J-XX:CMSInitiatingOccupancyFraction=90 \
--J-XX:+CMSIncrementalMode \
--J-XX:+UseCompressedOops \
"
