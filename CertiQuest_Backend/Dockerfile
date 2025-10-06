# Stage 1: Build
FROM maven:3.9.3-eclipse-temurin:21 AS build

WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Build the jar
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/target/CertiQuest-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
