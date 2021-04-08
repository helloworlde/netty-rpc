build:
	./gradlew clean build -x test

clean:
	./gradlew clean

test:
	./gradlew test

install:
	./gradlew clean build publishToMavenLocal -x test

