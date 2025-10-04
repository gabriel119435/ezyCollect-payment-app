# build stage
from maven:3.8.5-openjdk-17-slim as build
workdir /app

# copy pom.xml and download dependencies
copy pom.xml .
run mvn dependency:go-offline

# copy source code and build jar
copy src ./src
run mvn clean package -DskipTests

# run stage
from openjdk:17-jdk-slim
workdir /app
copy --from=build /app/target/*.jar app.jar
entrypoint ["java","-jar","/app/app.jar"]