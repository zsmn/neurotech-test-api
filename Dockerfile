FROM openjdk:11
EXPOSE 8080

ENV MAVEN_HOME /usr/lib/mvn
ENV PATH $MAVEN_HOME/bin:$PATH
ENV SPRING_DATASOURCE_URL jdbc:mysql://neurotech-test-api-mysql-container:3306/currency?useSSL=false&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true

ENV SPRING_DATASOURCE_USERNAME root
ENV SPRING_DATASOURCE_PASSWORD root

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]