FROM eclipse-temurin:21-jdk-alpine
RUN mkdir -p /app
VOLUME /tmp
COPY run.sh /app/run.sh
COPY target/*.jar /app/art.jar
ENTRYPOINT ["java","-jar","/app/art.jar"]

