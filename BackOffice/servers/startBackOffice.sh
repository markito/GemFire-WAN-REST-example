#!/bin/bash
################################
# Start back office servers
################################

. ./setEnv.sh

# default gemfire server listen address
SERVER_BIND=127.0.0.1

if [ ! -d "locator1" ]; then
	mkdir locator1
fi

if [ ! -d "backOfficeA" ]; then
	mkdir backOfficeA
fi

if [ ! -d "backOfficeB" ]; then
	mkdir backOfficeB
fi

echo "Starting locator..."

gfsh start locator --name=locator1 --dir=locator1 --J=-Dgemfire.jmx-manager-port=2099 --port=10333 --bind-address=$SERVER_BIND

echo "Starting backoffice servers..."

gfsh start server --server-bind-address=$SERVER_BIND --name=backOfficeA --locators=$SERVER_BIND[10333] --properties-file=conf/gemfire-bo.properties --dir=backOfficeA --initial-heap=$SERVER_HEAP --max-heap=$SERVER_HEAP \
--J="-XX:+UseConcMarkSweepGC" --J="-XX:+UseParNewGC" --server-port=50404 --J=-Dgemfire.http-service-port=8081 &

gfsh start server --server-bind-address=$SERVER_BIND --name=backOfficeB --locators=$SERVER_BIND[10333] --properties-file=conf/gemfire-bo.properties --dir=backOfficeB --initial-heap=$SERVER_HEAP --max-heap=$SERVER_HEAP \
--J="-XX:+UseConcMarkSweepGC" --J="-XX:+UseParNewGC" --server-port=50405 --J=-Dgemfire.http-service-port=8082

echo "Done."
