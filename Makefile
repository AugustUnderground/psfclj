target = ./target/psfconvert
wrapper = ./resources/wrapper.sh
uberjar = ./target/uberjar/*SNAPSHOT-standalone.jar

all:
	lein uberjar
	cat $(wrapper) $(uberjar) > $(target)
	chmod +x $(target)

clean:
	rm $(target)
	lein clean

maven:
	lein install
	lein pom
	mvn install

install:
	cp $(target) /usr/bin/psfconvert
