FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src
ENV MAVEN_OPTS="-Xmx2g -Xms1g"
RUN mvn -T 1C package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre-alpine as production
WORKDIR /app
COPY --from=build target/*.jar target/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/app.jar", "--spring.profiles.active=prod"]
