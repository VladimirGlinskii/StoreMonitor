FROM openjdk:21-jdk-slim-buster as build

COPY common/pom.xml /common/pom.xml
COPY common/src /common/src 

COPY base-api/mvnw /app/mvnw
COPY base-api/.mvn /app/.mvn
COPY base-api/pom.xml /app/pom.xml
COPY base-api/config /app/config
COPY base-api/src /app/src

WORKDIR /app
RUN ./mvnw install -f ../common
RUN ./mvnw -DskipITs install

ENTRYPOINT [ "ls" ]

FROM openjdk:21-jdk-slim-buster
COPY --from=build /app/target/base-api-0.0.1-SNAPSHOT.jar /app/lib/
COPY --from=build /app/config/application.properties /app/config/
COPY --from=build /app/config/application-docker.properties /app/config/
WORKDIR /app
ENTRYPOINT ["java", "-cp", "/app/config/:/app/lib/*", "org.springframework.boot.loader.launch.JarLauncher"]
CMD [ "--spring.profiles.active=docker" ]
