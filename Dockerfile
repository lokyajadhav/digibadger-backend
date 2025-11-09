# ------------ STAGE 1: Build the application ------------
    FROM eclipse-temurin:17-jdk AS builder

    # Set working directory
    WORKDIR /app
    
    # Copy everything into the container
    COPY . .
    
    # Build the application using Maven Wrapper
    RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests
    
    # ------------ STAGE 2: Create the final runtime image ------------
    FROM eclipse-temurin:17-jre-alpine
    
    # Metadata
    LABEL maintainer="lokya.naik@taashee.com"
    LABEL version="1.0"
    LABEL description="Badger backend image"
    
    
    
    # Set working directory inside the container
    WORKDIR /app
    
    # Copy only the JAR from the builder stage
    COPY --from=builder /app/target/*.jar app.jar
    
    # Expose the application port
    EXPOSE 8080
    
    # Optional Healthcheck using Spring Boot Actuator
    HEALTHCHECK --interval=30s --timeout=5s --start-period=10s CMD wget --spider http://10.21.34.171:8080/actuator/health || exit 1
    
    # Run the application
    ENTRYPOINT ["java", "-jar", "app.jar"]
    