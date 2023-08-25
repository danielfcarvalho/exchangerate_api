# Use an official Maven image as the base image
FROM maven:3.8.5-openjdk-17-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the project's POM file and download dependencies (caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the project files
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Use an official OpenJDK image as the base image for the runtime container
FROM eclipse-temurin:17-jdk-focal

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the build stage to the runtime container
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar ./app.jar

# Expose the port that the Spring Boot application listens on
EXPOSE 8080

# Define the command to run the application
CMD ["java", "-jar", "app.jar"]
