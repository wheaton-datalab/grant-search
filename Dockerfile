# Base image: Maven with Java 21
FROM maven:3.9.6-eclipse-temurin-21

# Set working directory
WORKDIR /app

# Install Python and pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

# Copy the whole project
COPY . /app/

# Install Python dependencies from requirements.txt
#RUN pip3 install --no-cache-dir -r requirements.txt
RUN pip install --upgrade pip && pip install -r requirements.txt

# Build the Java app
RUN mvn clean install

# Expose the Spring Boot port
EXPOSE 8080

# Run the Java JAR (update JAR name if different)
CMD ["java", "-jar", "target/grants-harvester-1.0-SNAPSHOT.jar"]
