FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN apk add --no-cache bash
RUN chmod +x /wait-for-it.sh
EXPOSE 8761
# Utilise le script pour attendre que config-service:8888 soit prêt
ENTRYPOINT ["/wait-for-it.sh", "config-service:8888", "--", "java", "-jar", "app.jar"]