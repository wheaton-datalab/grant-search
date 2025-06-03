# Use an offcial Java 21 image
FROM eclipse-temurin:21-jdk

# Set working directory inside container
WORKDIR /app

# Copy your project files into container
COPY . .

# Build the app with Maven (you already have pom.xml)
RUN ./mvnw clean install || mvn clean install

EXPOSE 8080

# Run the built JAR
CMD ["java", "-jar", "target/grants-harvester-1.0-SNAPSHOT.jar"]