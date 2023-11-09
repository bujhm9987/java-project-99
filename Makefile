setup:
	npm install
	./gradlew wrapper --gradle-version 8.4
	./gradlew build

frontend:
	make -C frontend start

clean:
	./gradlew clean

build:
	./gradlew clean build

start:
	./gradlew bootRun --args='--spring.profiles.active=development'

start-prod:
	./gradlew bootRun --args='--spring.profiles.active=production'

install:
	./gradlew installDist

start-dist:
	./build/install/app/bin/app

lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

generate-migrations:
	./gradlew diffChangeLog

db-migrate:
	./gradlew update

.PHONY: build frontend