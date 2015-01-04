#!/bin/bash
################################
# Start point of sales servers
################################

. ./setEnv.sh

SERVER_BIND=127.0.0.1

if [ ! -d "pointOfSaleA" ]; then
	mkdir pointOfSaleA
fi

if [ ! -d "pointOfSaleB" ]; then
	mkdir pointOfSaleB
fi


echo "Starting Point of Sales servers..."

gfsh start server --server-bind-address=$SERVER_BIND --name=pointOfSaleA --properties-file=conf/gemfire-pos.properties --dir=pointOfSaleA --initial-heap=$SERVER_HEAP --max-heap=$SERVER_HEAP \
--J="-XX:+UseConcMarkSweepGC" --J="-XX:+UseParNewGC" --J=-Dgemfire.http-service-port=7071 --J=-Dgemfire.jmx-manager-port=1099 --J=-Dgemfire.jmx-manager=true --J=-Dgemfire.jmx-manager-start=true --server-port=40404  &


gfsh start server --server-bind-address=$SERVER_BIND --name=pointOfSaleB --properties-file=conf/gemfire-pos.properties --dir=pointOfSaleB --initial-heap=$SERVER_HEAP --max-heap=$SERVER_HEAP \
--J="-XX:+UseConcMarkSweepGC" --J="-XX:+UseParNewGC" --J=-Dgemfire.http-service-port=7072 --J=-Dgemfire.jmx-manager-port=1098 --J=-Dgemfire.jmx-manager=true --J=-Dgemfire.jmx-manager-start=true --server-port=40405  


echo "Done."
