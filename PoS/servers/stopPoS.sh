#!/bin/bash
##########################################
# Stop point of sale servers 
##########################################

. ./setEnv.sh

echo "Stopping PoS nodes..."

gfsh stop server --dir=pointOfSaleA
gfsh stop server --dir=pointOfSaleB

echo "Done."