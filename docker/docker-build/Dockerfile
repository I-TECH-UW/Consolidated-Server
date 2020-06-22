FROM maven:3-jdk-11

ADD ./pom.xml /itech-fhirlibraries/
WORKDIR /itech-fhirlibraries

ADD ./fhir-core/pom.xml ./fhir-core/
ADD ./fhir-core/src ./fhir-core/src

ADD ./datasubscriber-core/pom.xml ./datasubscriber-core/
ADD ./datasubscriber-core/src ./datasubscriber-core/src/

ADD ./datarequest-core/pom.xml ./datarequest-core/
ADD ./datarequest-core/src ./datarequest-core/src/

ADD ./datarequest-api/pom.xml ./datarequest-api/
ADD ./datarequest-api/src ./datarequest-api/src/

ADD ./dataimport-core/pom.xml ./dataimport-core/
ADD ./dataimport-core/src ./dataimport-core/src/

ADD ./dataimport-api/pom.xml ./dataimport-api/
ADD ./dataimport-api/src ./dataimport-api/src/

ADD ./dataimport-webapp/pom.xml ./dataimport-webapp/
ADD ./dataimport-webapp/src ./dataimport-webapp/src/

RUN mvn clean install -DskipTests

CMD ["/bin/echo","Hello World!"]