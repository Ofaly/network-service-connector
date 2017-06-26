# Distributed service injector.

This library allows you to easy create communication between different services which run on different hosts. It uses the Apache zookeeper to find location of services.

First the application.properties file needs to be created in classpath. It should contain address to the zookeeper server and port which will be used to call  services.

zookeeper.connection.host=[hostname:port] // connection string to zookeeper server.
