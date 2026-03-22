# Stage 1: Build the application using Gradle
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy the Gradle wrapper and configuration files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Ensure the Gradle wrapper is executable
RUN chmod +x ./gradlew

# Download dependencies (this step will be cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon || true

# Copy the source code
COPY src src

# Build the application (skipping tests for a faster build)
RUN ./gradlew clean bootJar -x test --no-daemon

# Stage 2: Create the production image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root user for better security in production
RUN groupadd -r spring && useradd -r -g spring spring

# Create data directory and set permissions for the H2 database
RUN mkdir -p /app/data && chown spring:spring /app/data

# Switch to the non-root user
USER spring:spring

# Copy the built JAR from the build stage
COPY --from=build --chown=spring:spring /app/build/libs/*.jar app.jar

# Define a volume for the H2 database data directory to persist data across container restarts
VOLUME /app/data

# Expose the application port
EXPOSE 8080

# Environment variables that can be overridden at runtime
ENV SPRING_PROFILES_ACTIVE=prod
ENV IGDB_CLIENT_ID=
ENV IGDB_CLIENT_SECRET=
ENV GGDEALS_API_KEY=
ENV GGDEALS_REGION=es

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
