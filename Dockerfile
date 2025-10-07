# Étape 1: Build - Compilation de l'application
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (mise en cache des layers Docker)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler l'application et créer le JAR
RUN mvn clean package -DskipTests -B

# Étape 2: Runtime - Image finale légère
FROM eclipse-temurin:17-jre-alpine

# Métadonnées de l'image
LABEL maintainer="Bank System <support@banksystem.com>"
LABEL description="Bank Management System - API REST with Spring Boot"
LABEL version="1.0.0"

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S bankapp && \
    adduser -u 1001 -S bankapp -G bankapp

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR compilé depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Créer le répertoire pour les logs
RUN mkdir -p /app/logs && \
    chown -R bankapp:bankapp /app

# Changer vers l'utilisateur non-root
USER bankapp

# Exposer le port 8080
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xms512m -Xmx1024m" \
    SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod

# Healthcheck pour vérifier que l'application est en cours d'exécution
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Commande de démarrage de l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
