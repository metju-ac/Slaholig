# Stage 1: Build
FROM gradle:8.10-jdk21-corretto AS build

WORKDIR /application

# Copy Gradle build files from root
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copy shared module (required dependency)
COPY shared ./shared

# Copy this service
COPY courier-service ./courier-service

# Build only this service
RUN gradle :courier-service:clean :courier-service:bootJar --no-daemon

# Verify the output
RUN ls -l courier-service/build/libs

# Stage 2: Runtime
FROM amazoncorretto:21.0.9-alpine AS run

RUN adduser -D -s /bin/false app-user

# Copy the built jar
COPY --from=build --chown=app-user:app-user /application/courier-service/build/libs/*.jar app.jar

# Copy axon config from root
COPY axon ./axon

EXPOSE 8080

USER app-user
CMD ["java", "-jar", "app.jar", "--spring.config.additional-location=/config/courier-service/"]
