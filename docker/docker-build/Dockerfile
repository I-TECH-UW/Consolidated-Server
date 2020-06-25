FROM maven:3-jdk-11

ADD ./pom.xml /itech-fhirlibraries/
WORKDIR /itech-fhirlibraries

ADD ./fhir-core/pom.xml ./fhir-core/
ADD ./fhir-core/src ./fhir-core/src

ADD ./fhir-proxy-webapp/pom.xml ./fhir-proxy-webapp/
ADD ./fhir-proxy-webapp/src ./fhir-proxy-webapp/src

ADD ./fhir-server-management-webapp/pom.xml ./fhir-server-management-webapp/
ADD ./fhir-server-management-webapp/src ./fhir-server-management-webapp/src

RUN mvn clean install -DskipTests

CMD ["/bin/echo","Hello World!"]