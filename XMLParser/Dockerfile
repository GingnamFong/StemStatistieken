# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better Docker cache
COPY pom.xml .

# Download all dependencies
RUN mvn dependency:go-offline -B

# Copy source code and build JAR
COPY src ./src

# Build JAR file without tests for speed
RUN mvn clean package -DskipTests


# Runtime stage
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create data directory for Azure Files mount
RUN mkdir -p /home/data

# Java memory settings
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV H2_DATABASE_URL="jdbc:h2:file:/home/data/election-database;DB_CLOSE_DELAY=-1"

# Expose port 8081
EXPOSE 8081

# Start Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

