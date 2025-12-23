FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY commons commons
COPY payments payments

WORKDIR /workspace/payments
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

RUN useradd -r -u 10001 -g root -d /app -s /usr/sbin/nologin appuser

COPY --from=build /workspace/payments/build/libs/*-all.jar /app/app.jar
RUN chown -R 10001:0 /app

USER 10001
EXPOSE 8082

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
