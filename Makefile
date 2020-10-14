target = psfconvert

all:
	lein uberjar
	cat ./resources/wrapper.sh ./target/uberjar/*SNAPSHOT-standalone.jar > ./target/$(target)
	chmod +x ./target/psfconvert

clean:
	rm ./target/$(target)
	lein clean
