# Shared build environment: Java 11 (Temurin) + Maven, matching CI.
FROM maven:3.9.9-eclipse-temurin-11

WORKDIR /workspace

# Pre-fetch dependencies and plugins so the first test run is fast and
# works offline. Only the POM is copied; source is mounted at runtime.
COPY pom.xml checkstyle.xml ./
RUN mvn -B -q dependency:go-offline \
    && mvn -B -q dependency:resolve-plugins \
    && rm -f pom.xml checkstyle.xml

CMD ["mvn", "-B", "test"]
