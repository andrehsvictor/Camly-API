FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw clean package -DskipTests

FROM gcr.io/distroless/java21-debian12:latest
ENV SPRING_PROFILES_ACTIVE="prod"
WORKDIR /app
COPY --from=build /app/target/*.jar camly.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/camly.jar"]