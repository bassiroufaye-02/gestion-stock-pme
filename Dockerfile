# ======================================================
# Stage 1 : Build avec Maven
# ======================================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copier pom.xml d'abord pour profiter du cache Docker
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copier les sources et compiler
COPY src ./src
RUN mvn package -DskipTests -q

# ======================================================
# Stage 2 : Image finale légère avec JRE seulement
# ======================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Equipe PME <contact@pme.com>"
LABEL description="Gestion Stock PME - Spring Boot API"

# Utilisateur non-root pour la sécurité
RUN addgroup -S pme && adduser -S pmeapp -G pme

WORKDIR /app

# Copier uniquement le JAR depuis le stage builder
COPY --from=builder /app/target/*.jar app.jar

# Changer le propriétaire
RUN chown pmeapp:pme app.jar

USER pmeapp

EXPOSE 8080

# Paramètres JVM optimisés pour conteneur
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
