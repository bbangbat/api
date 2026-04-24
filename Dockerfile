FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./gradlew :app:bootJar --no-daemon

FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
