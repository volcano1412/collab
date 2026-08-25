# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S collab \
    && adduser -S -G collab -u 10001 collab

WORKDIR /app
COPY --from=builder --chown=collab:collab /workspace/build/libs/*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"

USER collab
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl --fail --silent http://localhost:8080/v3/api-docs > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

