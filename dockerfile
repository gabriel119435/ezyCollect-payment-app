# build stage
from maven:3.9.11-amazoncorretto-21-alpine as build
workdir /app

# copy pom.xml and download dependencies
copy pom.xml .
run mvn dependency:go-offline

# copy source code and build jar
copy src ./src
run mvn clean package

# run stage
from amazoncorretto:21-alpine-jdk
workdir /app
copy --from=build /app/target/*.jar app.jar
entrypoint ["java","-jar","/app/app.jar"]