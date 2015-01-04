#!/bin/bash
##########
# Clean up and restart
##########
./stopPoS.sh

# remove all files under point of sale instances
rm -rf pointOfSale*/*

./startPoS.sh