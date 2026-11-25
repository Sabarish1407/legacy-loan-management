FROM eclipse-temurin:17-jre

RUN groupadd -r app && useradd -r -g app app

WORKDIR /app

COPY --chown=app:app target/legacy-loan-management-*.jar /app/app.jar

USER app

ENV SPRING_PROFILES_ACTIVE=demo

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
