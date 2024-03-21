FROM openjdk:17-jdk-slim-buster

ADD target/*.jar /app/lib/
ADD config/application.properties /app/config/
ADD config/application-docker.properties /app/config/
WORKDIR /app

ENTRYPOINT ["java", "-cp", "/app/config/:/app/lib/*", "org.springframework.boot.loader.launch.JarLauncher"]
CMD [ "--spring.profiles.active=docker" ]
