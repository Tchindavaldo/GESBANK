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

# Exposer le port (Cloud Run injecte automatiquement la variable PORT)
EXPOSE 8080

# ============================================
# VARIABLES D'ENVIRONNEMENT SUPABASE
# ============================================
# ⚠️ IMPORTANT : Remplacez [YOUR-PASSWORD] par votre vrai mot de passe Supabase !
# Ces valeurs sont configurées en dur pour un déploiement automatique sur Cloud Run

# Configuration Base de Données Supabase
ENV DB_HOST=db.okxwulsfwuaczhtisjsb.supabase.co \
    DB_PORT=5432 \
    DB_NAME=postgres \
    DB_USERNAME=postgres \
    DB_PASSWORD=Nemerinho2001

# Configuration JWT
ENV JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Configuration Spring
ENV SPRING_PROFILES_ACTIVE=prod

# Configuration JVM optimisée pour Cloud Run
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Configuration du port serveur
ENV SERVER_PORT=8080

# ============================================
# COMMANDE DE DÉMARRAGE
# ============================================
# Cloud Run injecte automatiquement la variable PORT
# Priorité: PORT (Cloud Run) > SERVER_PORT > 8080 (par défaut)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-${SERVER_PORT:-8080}} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

# ============================================
# INSTRUCTIONS POUR DÉPLOIEMENT
# ============================================
# 1. Remplacez [YOUR-PASSWORD] par votre mot de passe Supabase (ligne 52)
# 2. Build l'image: docker build -t votre-username/bank-system:latest .
# 3. Push sur Docker Hub: docker push votre-username/bank-system:latest
# 4. Sur Cloud Run, déployez simplement l'image sans configurer de variables !
# 5. L'application se connectera automatiquement à Supabase
# ============================================
