# Use Maven with Java 21 base image
FROM maven:3.9.6-eclipse-temurin-21

# Set working directory to /app
WORKDIR /app

# Install Python and pip
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    which python3 && \
    rm -rf /var/lib/apt/lists/*

# Copy project into /app
COPY . .

# Ensure Python requirements file is present and install
RUN pip3 install --upgrade pip && pip3 install -r /app/requirements.txt

# Build the Spring Boot application
RUN mvn clean install

# Expose the app port
EXPOSE 8080

RUN echo "Listing contents of /app:" && ls -R /app

# Launch the app
CMD ["java", "-jar", "/app/target/grants-harvester-1.0-SNAPSHOT.jar"]
