# Этап 1: Сборка .jar
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle build -x test --no-daemon

# Этап 2: Запуск .jar
FROM openjdk:17-jdk-alpine
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]