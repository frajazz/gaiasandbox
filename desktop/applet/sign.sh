#!/bin/bash

echo "Number of arguments: $#"

if [ $# -ne 3 ]; then
	echo "Usage: $0 manifest_file keystore_file directory" >&2
	exit 1
fi

MF=$1
KEYSTORE=$2
DIRECTORY=$3

for f in $DIRECTORY/*.jar
do
	if [ -n "$MF" ]; then
		# MF not empty, we update manifests
		echo "Updating manifest in file $f with $MF"
		jar uvfm $f $MF
	fi 
	echo "Signing file - $f"
	jarsigner -keystore $KEYSTORE -storepass gdxpassword $f gdxkey
done