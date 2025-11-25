# Bouw fase
FROM maven:3.9-eclipse-temurin-21 AS build

# Werk in de app map
WORKDIR /app

# Kopieer pom.xml eerst voor betere Docker cache
COPY pom.xml .

# Download alle dependencies zonder internet
RUN mvn dependency:go-offline -B

# Kopieer alle source code en bouw de JAR
COPY src ./src

# Bouw de JAR file zonder tests voor snelheid
RUN mvn clean package -DskipTests


# Run fase
FROM eclipse-temurin:21-jre

# Werk in app map
WORKDIR /app

# Kopieer de gebouwde JAR van de build fase
COPY --from=build /app/target/*.jar app.jar

# Java geheugen instellingen
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Poort 8081 alleen toegankelijk maken
EXPOSE 8081

# Start de Spring Boot applicatie
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

