FROM openjdk:8-jdk
COPY target/*.jar app.jar
ENTRYPOINT java $JAVA_OPTS -jar $CONFIG app.jar