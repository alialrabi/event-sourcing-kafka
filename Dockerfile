FROM gradle:8.14-jdk17 AS build

WORKDIR /app

COPY ../build.gradle .
COPY ../settings.gradle .
COPY ../src ./src

RUN gradle bootJar

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]