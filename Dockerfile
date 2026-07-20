FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S orbitgroup && adduser -S orbituser -G orbitgroup
USER orbituser:orbitgroup
WORKDIR /app
COPY build/libs/orbit-telemetry.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseZGC", "-Xmx256m", "-jar", "/app/app.jar"]
