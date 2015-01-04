#!/bin/bash
##########################################
# Stop backoffice servers without forcing
##########################################

. ./setEnv.sh

echo "Stopping nodes and locator..."

gfsh stop server --dir=backOfficeA
gfsh stop server --dir=backOfficeB
gfsh stop locator --dir=locator1

echo "Done."
