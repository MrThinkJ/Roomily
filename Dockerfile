FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY ./pom.xml ./pom.xml

RUN apk add --no-cache bash
SHELL ["/bin/bash", "-c"]
RUN mvn clean dependency:go-offline -B -Dmaven.repo.local=/root/.m2/repository

COPY ./src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app
COPY --from=build /app/target/*.jar target/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/app.jar", "--spring.profiles.active=prod"]
