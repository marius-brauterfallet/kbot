# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine as builder

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
RUN ./gradlew build --stacktrace --no-daemon

# Copy the source code
COPY src src

# Build the application
RUN ./gradlew shadowJar --stacktrace --no-daemon

# Use a smaller base image for the final image
FROM eclipse-temurin:21-jre-alpine as runner

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the builder stage
COPY --from=builder /app/build/libs/kbot-*-all.jar /app/kbot.jar

# Set the default command to run the app
CMD ["java", "-jar", "/app/kbot.jar"]