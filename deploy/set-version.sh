#!/bin/sh -e

WORK_DIR=`readlink -f $0`
WORK_DIR=`dirname "$WORK_DIR"`
WORK_DIR=`dirname "$WORK_DIR"`

cd "$WORK_DIR"

VERSION=$1
SED_SCRIPT="s|<version>.*</version><!-- VERSION -->|<version>${VERSION}</version><!-- VERSION -->|g"
POMS="pom.xml"

for p in $POMS; do
	sed -e "$SED_SCRIPT" $p > ${p}.tmp
	mv ${p}.tmp $p
done
