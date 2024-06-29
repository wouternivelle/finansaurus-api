FROM eclipse-temurin:21
MAINTAINER nivelle.io
COPY build/libs/*.jar app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
