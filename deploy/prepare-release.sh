#!/bin/sh -ex

WORK_DIR=`readlink -f $0`
WORK_DIR=`dirname "$WORK_DIR"`
WORK_DIR=`dirname "$WORK_DIR"`

cd "$WORK_DIR"

VERSION=$1

if [ -z "$VERSION" ]; then
	echo "you must specify release version"
	exit 1
fi

mkdir src/sql/$VERSION || true
git add src/sql/$VERSION

if [ `find src/sql/SNAPSHOT -type f | wc -l` != "0" ]; then
	git mv src/sql/SNAPSHOT/* src/sql/$VERSION
fi
./deploy/set-version.sh $VERSION
dch -v "${VERSION}-1" "release $VERSION"

git commit -am "prepare release $VERSION"
git tag -f $VERSION
