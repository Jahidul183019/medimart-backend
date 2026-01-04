# Use OpenJDK 21 base image (Eclipse Temurin)
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot jar file from the target directory
COPY target/medimart-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the Spring Boot application will run on (default 8080)
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
