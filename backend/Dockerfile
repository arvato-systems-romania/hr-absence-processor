FROM maven:3.8.6-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app

RUN apt-get update && \
    apt-get install -y fonts-dejavu-core fontconfig && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/data/input /app/logs /tmp
COPY --from=build /app/target/hr-absence-processor-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]