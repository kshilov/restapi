all:
	mvn clean install

clean:
	mvn clean

check:
	mvn verify

install:
	install -D -m 644 target/rest.jar $(DESTDIR)/usr/share/backend/backend.jar
	install -D -m 644 src/etc/settings.properties $(DESTDIR)/etc/backend/settings.properties
	install -D -m 755 backend $(DESTDIR)/usr/bin/backend
	install -D -m 755 ymlimport $(DESTDIR)/usr/bin/ymlimport
	unzip target/rest.jar version.properties -d $(DESTDIR)/usr/share/backend
	mkdir -p $(DESTDIR)/usr/share/backend/db-migrate
	cp -r src/sql/* $(DESTDIR)/usr/share/backend/db-migrate
	install -D -m 755 deploy/db-migrate.sh $(DESTDIR)/usr/share/backend
	mkdir -p $(DESTDIR)/var/lib/backend/banners
	chmod -R 666 $(DESTDIR)/var/lib/backend/banners

dev-build:
	mvn package -DskipTests=true

dev-build-t:
	mvn package

dev-run:
	cd target && java -DsettingsFile=/etc/backend/settings.properties -jar rest.jar
