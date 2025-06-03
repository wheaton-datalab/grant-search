# Use official Maven with Java 21
FROM maven:3.9.6-eclipse-temurin-21

# Set working directory inside container
WORKDIR /app

# Copy your project files into container
COPY . .

# Build the app with Maven (you already have pom.xml)
RUN mvn clean install

EXPOSE 8080

# Run the built JAR
CMD ["java", "-jar", "target/grants-harvester-1.0-SNAPSHOT.jar"]