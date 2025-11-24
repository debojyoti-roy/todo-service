# Use an official OpenJDK image as a base
FROM openjdk:17.0.2-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
# !!! IMPORTANT: VERIFY THIS FILENAME MATCHES YOUR BUILD OUTPUT !!!
COPY build/libs/todo-service-0.0.1-SNAPSHOT.jar todo-service.jar

# Expose the application port (8081) and the H2 TCP port (9092)
EXPOSE 8081 9092

# Set the entry point to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "todo-service.jar"]