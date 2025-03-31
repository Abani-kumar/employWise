# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Grant execution permission to Maven wrapper
RUN chmod +x mvnw

# Build the project
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run the application
CMD ["java", "-jar", "target/*.jar"]
