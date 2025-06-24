# Étape de build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape d'exécution
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN apk add --no-cache bash
RUN chmod +x /wait-for-it.sh

EXPOSE 8080

# Utilise le script pour attendre toutes les dépendances avant de lancer l'application
ENTRYPOINT ["/bin/bash", "-c", "/wait-for-it.sh config-service:8888 -t 60 --strict && /wait-for-it.sh postgres-auth:5432 -t 60 --strict && /wait-for-it.sh kafka:9092 -t 60 --strict && exec java -jar app.jar"]
