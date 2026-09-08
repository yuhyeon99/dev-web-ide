FROM node:20-bookworm-slim AS node-runtime
FROM python:3.12-bookworm AS python-runtime

FROM eclipse-temurin:25-jdk

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends g++ make \
    && rm -rf /var/lib/apt/lists/*

COPY --from=python-runtime /usr/local /usr/local
COPY --from=node-runtime /usr/local/bin/node /usr/local/bin/node

RUN groupadd --system app && useradd --system --gid app app

COPY build/libs/*.jar /tmp/
RUN find /tmp -name "*.jar" ! -name "*plain.jar" -exec cp {} /app/app.jar \; \
    && rm -f /tmp/*.jar \
    && mkdir -p /app/storage \
    && chown -R app:app /app

USER app

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
