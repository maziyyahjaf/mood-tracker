FROM eclipse-temurin:23-jdk AS builder

ARG COMPILE_DIR=/code_folder

WORKDIR ${COMPILE_DIR}

# Copy all necessary files for Maven build
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copy the source code
COPY src src

# Build the project with Maven, skipping the tests
RUN ./mvnw clean package -Dmaven.test.skip=true

# Expose the server port as an environment variable
ENV SERVER_PORT=1471

# Expose the port for external access
EXPOSE ${SERVER_PORT}

# Entrypoint to run the application
ENTRYPOINT ["java",  "-jar", "target/mood-tracker-0.0.1-SNAPSHOT.jar"]

# Stage 2: Production stage
FROM eclipse-temurin:23-jdk

# Argument for the directory where the final JAR will be deployed
ARG DEPLOY_DIR=/APP

# Set the working directory for the final stage
WORKDIR ${DEPLOY_DIR}

# Copy the build JAR file from the build stage to the production stage
COPY --from=builder /code_folder/target/mood-tracker-0.0.1-SNAPSHOT.jar mood-tracker.jar

# Expose the server port as an environment variable
ENV SERVER_PORT=1471

# Expose the port for external access
EXPOSE ${SERVER_PORT}

# Entrypoint to run the final applicatiom
ENTRYPOINT ["java", "-jar",  "mood-tracker.jar"]
CMD ["--server.port=1471"]