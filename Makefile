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

dev-build:
	mvn package -DskipTests=true

dev-build-t:
	mvn package

dev-run:
	cd target && java -DsettingsFile=/etc/backend/settings.properties -jar rest.jar
