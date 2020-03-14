FROM itech-fhir-libraries:latest as maven-build
COPY ./pom.xml /dataimport-webapp/
COPY ./src /dataimport-webapp/src/
WORKDIR /dataimport-webapp/
RUN mvn clean install -DskipTests

#switch to this once their is an official jdk 11 alpine image
#FROM openjdk:11-jre-alpine
FROM alpine
RUN apk --no-cache add openjdk11

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=maven-build /dataimport-webapp/target/*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
