#switch to this once their is an official jdk 11 alpine image
#FROM openjdk:11-jre-alpine
FROM alpine
RUN apk --no-cache add openjdk11
RUN apk add --no-cache bash

RUN addgroup --gid 8443 -S spring && adduser --uid 8443 -S spring-user -G spring
USER spring-user:spring

ADD ./target/*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

