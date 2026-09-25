FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f backend/pom.xml clean package -Dmaven.test.skip=true -Djava.version=21

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /workspace/backend/target/backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
