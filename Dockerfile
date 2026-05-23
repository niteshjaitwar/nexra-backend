# syntax=docker/dockerfile:1.7

FROM maven:3.9.11-eclipse-temurin-25@sha256:8aa64732682b9021d15a51265552ba77b0e0d96d5f993196183eee4020ab8285 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

FROM eclipse-temurin:25-jre@sha256:98236ffdfb61cff3fc4f8e40e5b80460a5a4c35a0f56a5b4182a926844a7db49
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN addgroup --system nexra && adduser --system --ingroup nexra nexra

COPY --from=build /workspace/target/*.jar /app/nexra.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 CMD curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health/readiness > /dev/null || exit 1
USER nexra

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/nexra.jar"]
