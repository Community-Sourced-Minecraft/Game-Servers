FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY . .
# build gradle project
RUN ./gradlew build
RUN mkdir -p /app/build/libs
RUN find . -path "*/build/libs/*-all.jar" -exec cp {} /app/build/libs \;


FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar /app/
