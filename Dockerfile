# Use base image with Java 21 and Maven
FROM maven:3.9.6-eclipse-temurin-21

# Set working directory inside container
WORKDIR /app

# Install Python3 and pip, create symlink so "python" works
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    echo "✅ Python installed:" && which python && python --version

# Copy everything into the container image
COPY . /app/

# Install Python dependencies
RUN pip install --upgrade pip && pip install -r requirements.txt

# Build the Spring Boot application
RUN mvn clean install

# Show what tools are present (debug)
RUN echo "✅ Listing /app/tools/" && ls -l /app/tools

# Expose port for Spring Boot
EXPOSE 8080

# Start the Spring Boot app
CMD ["java", "-jar", "target/grants-harvester-1.0-SNAPSHOT.jar"]