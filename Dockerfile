FROM eclipse-temurin:17-jdk-jammy AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY gradle $APP_HOME/gradle
COPY gradlew .
RUN chmod +x gradlew && ./gradlew
RUN ./gradlew build || return 0
COPY . .
RUN ./gradlew clean build

# Actual container
FROM eclipse-temurin:17.0.10_7-jre-ubi9-minimal
ENV ARTIFACT_NAME=web.socket.server-0.0.1-SNAPSHOT.jar
ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/$ARTIFACT_NAME .

EXPOSE 8080
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}