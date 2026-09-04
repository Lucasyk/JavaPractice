FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

COPY src src

RUN ./gradlew clean bootJar --no-daemon

RUN JAR=$(find build/libs -name "*.jar" ! -name "*-plain.jar" | head -n 1) \
    && cp "$JAR" /app/app.jar


FROM eclipse-temurin:26-jre

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]