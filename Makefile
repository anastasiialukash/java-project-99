.PHONY: setup build test

setup:
	cd app && ./gradlew wrapper --gradle-version=8.14.3 --distribution-type=bin

build:
	cd app && ./gradlew build

test:
	cd app && ./gradlew test