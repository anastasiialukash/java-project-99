FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradlew .

RUN ./gradlew --no-daemon dependencies

COPY src src
COPY config config

RUN ./gradlew --no-daemon bootJar

ENV JAVA_OPTS="-Xmx512M -Xms512M"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar build/libs/java-project-99-0.0.1-SNAPSHOT.jar"]