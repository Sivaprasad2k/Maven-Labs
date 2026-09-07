# Stage 1: Build executable JAR using Maven wrapper
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper files and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make mvnw executable
RUN chmod +x mvnw

# Copy source code and build package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal Java 17 runtime container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built executable JAR from builder stage
COPY --from=builder /app/target/oddly-specific-1.0-SNAPSHOT.jar app.jar

# Expose default port
EXPOSE 8080

# Execute application JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
