#!/bin/bash -e

SETTINGS_FILE='/etc/backend/settings.properties'
PG_URL=`grep 'hibernate.connection.url' $SETTINGS_FILE | grep -o '\/\/.*\/.*'`
PG_HOST=`echo "$PG_URL" | cut -d'/' -f3 | cut -d':' -f1`
PG_DB=`echo "$PG_URL" | cut -d'/' -f4`
PG_USER=`grep 'hibernate.connection.username' $SETTINGS_FILE | grep -o '[^ ]*$'`
export PGPASSWORD=`grep 'hibernate.connection.password' $SETTINGS_FILE | grep -o '[^ ]*$'`

echo "pg command is psql -U "$PG_USER" -h "$PG_HOST" "$PG_DB""

psql -U "$PG_USER" -h "$PG_HOST" "$PG_DB" \
	-c "create table version ( value text );" || true

INSTALLED_VERSION=`psql -U "$PG_USER" -h "$PG_HOST" "$PG_DB" -c "copy (select value from version) to stdout"`
CURRENT_VERSION=`grep -o '[^ ]*$' /usr/share/backend/version.properties`
if [ "$CURRENT_VERSION" != "${CURRENT_VERSION%-SNAPSHOT}" ]; then
	CURRENT_VERSION='SNAPSHOT'
fi

echo "installed version is "$INSTALLED_VERSION""
echo "package version is "$CURRENT_VERSION""

for DIR in /usr/share/backend/db-migrate/*; do
	TEST_VER=`basename "$DIR"`
	if [[ "$TEST_VER" > "$INSTALLED_VERSION" ]]; then
		echo "applying all script for version "$TEST_VER""
		if [ `find "$DIR" -type f | wc -l` != "0" ]; then
			for SCRIPT in ${DIR}/*; do
				echo "applying script "$SCRIPT""
				psql -U "$PG_USER" -h "$PG_HOST" "$PG_DB" -f "$SCRIPT"
			done
		fi
	else
		echo "skipping all scripts vor version "$TEST_VER""
	fi
done

echo "updating version in db to value "$CURRENT_VERSION""
psql -U "$PG_USER" -h "$PG_HOST" "$PG_DB" -c "insert into version(value) select '$CURRENT_VERSION' \
	where not exists (select 1 from version)"
psql -U "$PG_USER" -h "$PG_HOST" "$PG_DB" -c "update version set value='"$CURRENT_VERSION"'"
