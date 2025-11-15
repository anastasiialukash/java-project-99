FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY app/gradle gradle
COPY app/build.gradle.kts .
COPY app/settings.gradle.kts .
COPY app/gradlew .

RUN ./gradlew --no-daemon dependencies

COPY app/src src
COPY app/config config

RUN ./gradlew --no-daemon bootJar

ENV JAVA_OPTS="-Xmx512M -Xms512M"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar build/libs/app-0.0.1-SNAPSHOT.jar"]