FROM azul/zulu-openjdk-alpine:11-jre
ARG JAVA_OPTS="-Xss512k -noverify"
ADD target/openwms-tms-transportation-exec.jar app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
