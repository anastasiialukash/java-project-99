.PHONY: setup build test

setup:
	./gradlew wrapper --gradle-version=8.14.3 --distribution-type=bin

build:
	./gradlew build

test:
	./gradlew clean test jacocoTestReport sonarqube -Dsonar.login=$(SONAR_TOKEN)