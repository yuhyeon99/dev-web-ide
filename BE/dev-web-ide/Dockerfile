FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /workspace/build/libs/*.jar /tmp/
RUN find /tmp -name "*.jar" ! -name "*plain.jar" -exec cp {} /app/app.jar \; \
    && rm -f /tmp/*.jar \
    && mkdir -p /app/storage \
    && chown -R app:app /app

USER app

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
