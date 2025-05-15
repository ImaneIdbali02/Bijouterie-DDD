# Étape de build
FROM maven:3.8.4-openjdk-21-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape d'exécution
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]