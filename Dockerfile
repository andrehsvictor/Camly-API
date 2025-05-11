FROM ghcr.io/graalvm/native-image-community:24.0.1 AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src ./src
RUN ./mvnw -Pnative native:compile -DskipTests

FROM alpine:3.21.3 AS runtime
WORKDIR /app
COPY --from=builder /app/target/camly-api .
EXPOSE 8080
ENTRYPOINT ["./camly-api"]