#!/bin/sh -ex

WORK_DIR=`readlink -f $0`
WORK_DIR=`dirname "$WORK_DIR"`
WORK_DIR=`dirname "$WORK_DIR"`

cd "$WORK_DIR"

VERSION=$1

if [ -z "$VERSION" ]; then
	echo "you must specify next development version"
	exit 1
fi

./deploy/set-version.sh $VERSION

git commit -am "prepare next development $VERSION"

