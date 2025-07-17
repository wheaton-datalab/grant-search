# Use Maven base image with Java 21
FROM maven:3.9.6-eclipse-temurin-21

# Set working directory
WORKDIR /app

# Install Python and pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    python3 --version && pip3 --version && \
    rm -rf /var/lib/apt/lists/*

# Copy entire project into container
COPY . .

# Install Python dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Build the Java Spring Boot application
RUN mvn clean install

# Expose port for Spring Boot
EXPOSE 8080

# Default command
CMD ["java", "-jar", "target/grants-harvester-1.0-SNAPSHOT.jar"]
