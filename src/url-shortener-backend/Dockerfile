FROM gradle:8.13-jdk21 AS builder

WORKDIR /home/gradle/project
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew .
COPY --chown=gradle:gradle settings.gradle.kts build.gradle.kts ./

RUN gradle build -x test --no-daemon || true

COPY --chown=gradle:gradle src src
RUN gradle clean bootJar --no-daemon



FROM gcr.io/distroless/java21-debian12 as runtime
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

WORKDIR /app
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]