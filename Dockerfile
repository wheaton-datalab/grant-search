# Start with Java and Maven base image
FROM maven:3.9.6-eclipse-temurin-21

# Set working directory
WORKDIR /app

# Install Python and pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    ln -s /usr/bin/python3 /usr/bin/python

# Copy all project files into the image
COPY . .

# Install Python requirements
RUN pip install -r requirements.txt

# Build the Spring Boot app
RUN mvn clean package -DskipTests

# Expose app port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/grants-harvester-1.0-SNAPSHOT.jar"]
