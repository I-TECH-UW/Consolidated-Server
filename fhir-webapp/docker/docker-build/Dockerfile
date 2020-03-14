FROM maven:3-jdk-11
ADD ./pom.xml /fhir-core/
ADD ./src /fhir-core/src/
WORKDIR /fhir-core/
RUN mvn clean install -DskipTests

CMD ["/bin/echo","Hello World!"]