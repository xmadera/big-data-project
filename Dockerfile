#
# Build stage
#
FROM maven:latest as build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package


#
# Package stage
#
FROM dquintela/openjdk-8-jdk-alpine
COPY  --from=build /usr/src/app/target/big-data-crawler-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]