[![Build Status](https://travis-ci.org/thesolwind/network-service-connector.svg?branch=master)](https://travis-ci.org/thesolwind/network-service-connector)
[![codecov](https://codecov.io/gh/thesolwind/network-service-connector/branch/master/graph/badge.svg)](https://codecov.io/gh/thesolwind/network-service-connector)
# Network service connector

This library allows to easy connect between different services which ran on different hosts. It uses the Apache zookeeper to find location of services, the netty io to connnect beatween services and java serialization to serialize queries beatween services.

#### Maven central
```xml
<dependency>
    <groupId>io.solwind</groupId>
    <artifactId>network-service-connector</artifactId>
    <version>0.0.7</version>
</dependency>
```

The application.properties file needs to be created in classpath. 
It should contain address to the zookeeper server.

zookeeper.connection.host - connection string to zookeeper server.<br/>

### Example
1. Create application.properties with the following content
```properties
zookeeper.connection.host=127.0.0.1:2181
```

2. Create interface and class. All custom objects must be serializable.
```java
public interface ITestService {
    String text();
    Integer digit();
}

public class TestService implements ITestService {

    public String text() {
        return "Some text";
    }

    public Integer digit() {
        return 123;
    }
}
```

3. Cretae service registrar
```java
ServiceRegistrar registrar = Network.newServiceRegistrar("service-name", "host:port", new ZookeeperDiscoveryConnector(), new NettyIoRmiConnectorServer());
```
`service-name` is name of your service<br/>
`host:port` is host and port of your service for example you can use
```java
 InetAddress.getLocalHost().getHostAddress()
```
to know your host address in network.

4. Register service.
```java
registrar.register(new TestService(), "version1", "short service description");
```
so now zookeeper has record about ITestService, it includes interface name, host address and port.

5. Create registrar client.

Client application has to have the same properties file with zookeeper host.
```java
ServiceRegistrarClient clientRegistrar = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector());
```

6. Create service.
```java
ITestService testService = clientRegistrar.create(ITestService.class, "service-name", new NettyIoRmiConnectorClient());
```
`service-name` must be the same like in service registrar otherwise client is not gonna find service.
