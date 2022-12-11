FROM openjdk:17-alpine
MAINTAINER nivelle.io
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
