# --- ЭТАП 1: "СБОРКА" (Build Stage) ---
# 1. Берем "фундамент" с Java 21 (JDK) и Maven
FROM maven:3.9-eclipse-temurin-21 AS builder

# 2. Устанавливаем рабочую директорию внутри образа
WORKDIR /app

# 3. Копируем pom.xml, чтобы Docker мог кэшировать зависимости
COPY pom.xml .

# 4. Копируем остальной исходный код
COPY src ./src

# 5. Собираем приложение с помощью Maven
# (Мы отключаем тесты, чтобы сборка в Docker была быстрой)
RUN mvn package -DskipTests


# --- ЭТАП 2: "ЗАПУСК" (Run Stage) ---
# 6. Берем "чистый" образ. Нам нужен только JRE (Java Runtime), а не весь JDK
FROM eclipse-temurin:21-jre-jammy

USER root
RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists/*

# 7. Устанавливаем рабочую директорию
WORKDIR /app

# 8. Копируем *только* собранный .jar из этапа "builder"
COPY --from=builder /app/target/agora-api-1.0.jar app.jar

# 9. Говорим Docker, что наше приложение будет работать на порту 8080
EXPOSE 8080

# 10. Команда, которая запускает твое приложение
ENTRYPOINT ["java", "-jar", "app.jar"]