# Étape 1 : Build Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution légère
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# (Optionnel) Ajoute wait-for-it.sh si tu veux attendre config-service/eureka
# COPY wait-for-it.sh /wait-for-it.sh
# RUN apk add --no-cache bash
# RUN chmod +x /wait-for-it.sh

EXPOSE 8080

# Lancement direct (ou avec wait-for-it.sh si tu veux attendre les dépendances)
ENTRYPOINT ["java", "-jar", "app.jar"]
# Exemple avec wait-for-it.sh :
# ENTRYPOINT ["/bin/bash", "-c", "/wait-for-it.sh config-service:8888 -t 60 --strict && /wait-for-it.sh eureka-server:8761 -t 60 --strict && exec java -jar app.jar"]