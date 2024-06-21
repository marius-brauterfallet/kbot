# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the build.gradle and settings.gradle files
COPY build.gradle.kts settings.gradle.kts ./

# Copy the Gradle wrapper
COPY gradle gradle

# Copy the gradle wrapper script
COPY gradlew gradle.properties ./

# Ensure the gradle wrapper script is executable
RUN chmod +x ./gradlew

# Download dependencies and prepare the build
RUN ./gradlew build || return 0

# Copy the source code
COPY src src

# Build the application
RUN ./gradlew shadowJar

# Set the default command to run the app
CMD ["java", "-jar", "build/libs/KimmoBot-0.0.1-all.jar"]