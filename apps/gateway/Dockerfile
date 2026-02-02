# syntax=docker/dockerfile:1.6

# -------------build stage--------------------
FROM gradle:8.10.1-jdk21 AS build
WORKDIR /workspace

#1) copier les fichiers Gradle racine (neccessaires au multi-modules)
COPY settings.gradle.kts build.gradle.kts gradle.properties* /workspace/

#2) Copier uniquement le module cible
COPY apps/gateway /workspace/apps/gateway

#3) Build uniquement le jar du module
RUN gradle :apps:gateway:bootJar --no-daemon

#------------runtime stage -----------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Jar prduit par spring boot (bootJar)
COPY --from=build /workspace/apps/gateway/build/libs/*.jar /app/app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]