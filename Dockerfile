# Use a lightweight OpenJDK image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/payment-0.0.1-SNAPSHOT.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 