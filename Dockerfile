FROM openjdk:17-alpine
MAINTAINER nivelle.io
COPY build/libs/*.jar app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
