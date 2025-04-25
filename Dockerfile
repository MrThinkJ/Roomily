FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
COPY ./pom.xml ./pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B
COPY ./src ./src
ENV MAVEN_OPTS="-Xmx2g -Xms1g"
RUN --mount=type=cache,target=/root/.m2 \
    --mount=type=cache,target=/app/target \
    mvn -T 2C package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true

FROM eclipse-temurin:21-jre-alpine as production
WORKDIR /app
COPY --from=build target/*.jar target/app.jar
COPY --from=build ./src/main/resources/config/firebase_key.json resources/config/firebase_key.json
COPY --from=build ./src/main/resources/static/contract.html resources/static/contract.html
COPY --from=build ./src/main/resources/static/fonts/times.ttf resources/static/fonts/times.ttf
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/app.jar", "--spring.profiles.active=prod"]