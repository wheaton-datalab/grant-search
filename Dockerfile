# Use Maven with Java 21 base image
FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app

# Install Python and pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

# Copy project
COPY . .

# Install Python requirements
RUN pip install -r requirements.txt

# Build the Spring Boot app
RUN mvn clean install

EXPOSE 8080

CMD ["java", "-jar", "target/grants-harvester-1.0-SNAPSHOT.jar"]
