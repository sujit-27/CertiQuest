# Use a lightweight OpenJDK 17 image with Maven installed
FROM maven:3.9.3-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first to leverage Docker cache
COPY pom.xml .

# Copy the source code
COPY src ./src

# Build the Spring Boot app (skip tests for faster build)
RUN mvn clean package -DskipTests

# Use a slim OpenJDK image for running the app
FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/certiquest-backend.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
