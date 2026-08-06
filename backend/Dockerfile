FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /app

# Cache dependencies in their own layer so source-only changes don't re-download the internet.
COPY pom.xml ./
RUN mvn -q dependency:go-offline -B

COPY src ./src

EXPOSE 4001
CMD ["mvn", "spring-boot:run"]
