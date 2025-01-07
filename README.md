# Purpose
The TMS Transportation Service offers essential functionality of the Transport Order Management (TMS) in automatic warehouses. This is 
often referred to as Material Flow Controller (MFC). This includes for example the ability to create and run _automated_ `TransportOrders`.

# Resources
[![Build status](https://github.com/openwms/org.openwms.tms.transportation/actions/workflows/master-build.yml/badge.svg)](https://github.com/openwms/org.openwms.tms.transportation/actions/workflows/master-build.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Quality](https://sonarcloud.io/api/project_badges/measure?project=org.openwms:org.openwms.tms.transportation&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.openwms:org.openwms.tms.transportation)
[![Join the chat at https://gitter.im/openwms/org.openwms](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/openwms/org.openwms?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

**Find further Documentation on [Microservice Website](https://openwms.github.io/org.openwms.tms.transportation) or on the [wiki page](https://wiki.openwms.cloud/projects/tms-transportation-service/wiki/).**

# Build
Build a runnable fat jar with the execution of all unit and in-memory database integration tests, but without a required [RabbitMQ](https://www.rabbitmq.com)
server to run: 

```
$ ./mvnw package
```

To also build and run with [RabbitMQ](https://www.rabbitmq.com) support call:

```
$ ./mvnw package -DsurefireArgs=-Dspring.profiles.active=ASYNCHRONOUS,TEST
```

# Run

## Run On Command Line
After the binary has been built it can be started from command line. By default no other infrastructure services are required to run this
service.

Run in standalone mode:
```
$ java -jar target/openwms-tms-transportation-exec.jar
```

In a distributed environment the service configuration is fetched from the central [OpenWMS.org Configuration Service](https://github.com/spring-labs/org.openwms.configuration).
This behavior can be enabled by activating the Spring Profile `DISTRIBUTED`. Additionally, it makes sense to enable asynchronous
communication that requires a running [RabbitMQ](https://www.rabbitmq.com) instance - just add another profile `ASYNCHRONOUS`. If the latter is not applied all
asynchronous AMQP endpoints are disabled and the service does not send any events nor does it receive application events from remote
services. The AMQP protocol with the [RabbitMQ](https://www.rabbitmq.com) is currently the only supported message broker. But switching to others, like [HiveMQ (MQTT)](https://www.hivemq.com) 
or [Apacha Kafka](https://kafka.apache.org/), is not rocket science.

```
$ java -jar target/openwms-tms-transportation-exec.jar --spring.profiles.active=ASYNCHRONOUS,DISTRIBUTED
```
This requires a [RabbitMQ](https://www.rabbitmq.com) server running locally with default settings.

With these profiles applied the OpenWMS.org Configuration Service is tried to be discovered at service startup. The service fails to start
if no instance of the configuration service is available after a configured amount of retries.

## Run as Docker Container
Instead of building the software from the sources and run it as Java program on the JVM it can also be fetched as a Docker image from 
[Docker Hub](https://hub.docker.com/repository/docker/openwms/org.openwms.common.service) and started as a Docker container.

```
$ docker run openwms/org.openwms.tms.transportation:latest
```
