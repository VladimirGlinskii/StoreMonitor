FROM openjdk:21-jdk-slim-buster as build

COPY pom.xml /app/pom.xml
COPY common/pom.xml /app/common/pom.xml
COPY common/src /app/common/src

COPY base-api/mvnw /app/mvnw
COPY base-api/.mvn /app/.mvn
COPY base-api/pom.xml /app/base-api/pom.xml
COPY base-api/config /app/base-api/config
COPY base-api/src /app/base-api/src

WORKDIR /app
RUN ./mvnw -P base-api -DskipTests install

ENTRYPOINT [ "ls" ]

FROM openjdk:21-jdk-slim-buster

COPY --from=build /app/base-api/target/base-api-1.0.jar /app/lib/
COPY --from=build /app/base-api/config/application.properties /app/config/
COPY --from=build /app/base-api/config/application-docker.properties /app/config/

ARG PORT=80
EXPOSE ${PORT}

WORKDIR /app
ENTRYPOINT ["java", "-cp", "/app/config/:/app/lib/*", "org.springframework.boot.loader.launch.JarLauncher"]
CMD [ "--spring.profiles.active=docker" ]
