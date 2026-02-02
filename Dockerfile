# syntax=docker/dockerfile:1.6

# -------------build stage--------------------
FROM gradle:8.10.1-jdk21 AS build
WORKDIR /workspace

# Args: module Gradle + dossier module + jar pattern
ARG GRADLE_TASK
ARG MODULE_DIR

# 1) copier les fichiers Gradle racine (necessaires au multi-modules)
COPY settings.gradle.kts build.gradle.kts gradle.properties* /workspace/

# 2) Copier uniquement le module cible
COPY ${MODULE_DIR} /workspace/${MODULE_DIR}

# 3) Build uniquement le jar du module
RUN gradle ${GRADLE_TASK} --no-daemon

# ------------runtime stage -----------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Jar produit par spring boot (bootJar)
COPY --from=build /workspace/${MODULE_DIR}/build/libs/*.jar /app/app.jar

# Port expose (documentaire)
ARG APP_PORT=8080
EXPOSE ${APP_PORT}

ENTRYPOINT ["java","-jar","/app/app.jar"]