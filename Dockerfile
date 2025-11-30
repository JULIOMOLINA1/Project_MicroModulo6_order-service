# Dockerfile for Product Service
# Use Eclipse Temurin JRE 17 as the base image
FROM eclipse-temurin:17-jre

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the target directory to the container
COPY target/*.jar /app/order-service.jar

# Expose port 8083 for the Product Service
EXPOSE 8083

# Define the command to run the Product Service
CMD ["java", "-jar", "/app/order-service.jar"]