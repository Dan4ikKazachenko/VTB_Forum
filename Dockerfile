# Используем официальный образ OpenJDK 17 с Alpine Linux (легковесный)
FROM openjdk:17-jdk-alpine

# Копируем собранный jar в контейнер
COPY build/libs/Vtb-0.0.1-SNAPSHOT.jar app.jar

# Указываем команду запуска
ENTRYPOINT ["java", "-jar", "/app.jar"]