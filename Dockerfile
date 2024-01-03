# Docker directives
FROM maven:3-eclipse-temurin-21  

WORKDIR /app

# Everything after this is in /app
COPY mvnw .
COPY mvnw.cmd .
COPY pom.xml .
# Give directory name when copying a directory
COPY .mvn .mvn 
COPY src src

RUN mvn package -Dmaven.test.skip=true

ENV PORT=8080
ENV SPRING_REDIS_HOST=localhost SPRING_REDIS_PORT=6379
ENV SPRING_REDIS_USERNAME=NOT_SET SPRING_REDIS_PASSWORD=NOT_SET

EXPOSE ${PORT}

ENTRYPOINT SERVER_PORT=${PORT} java -jar target/mini-project-0.0.1-SNAPSHOT.jar