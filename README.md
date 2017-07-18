[![Build Status](https://travis-ci.org/thesolwind/network-service-connector.svg?branch=master)](https://travis-ci.org/thesolwind/network-service-connector)
[![codecov](https://codecov.io/gh/thesolwind/network-service-connector/branch/master/graph/badge.svg)](https://codecov.io/gh/thesolwind/network-service-connector)
# Network service connector

This library allows to easy connect between different services which ran on different hosts. It uses the Apache zookeeper to find location of services, the netty io to connnect beatween services and java serialization to serialize queries between services.

#### Maven central
```xml
<dependency>
    <groupId>io.solwind</groupId>
    <artifactId>network-service-connector</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Snapshot
```xml
<repository>
    <id>sonatype</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</repository>
<dependency>
    <groupId>io.solwind</groupId>
    <artifactId>network-service-connector</artifactId>
    <version>1.0.1-SNAPSHOT</version>
</dependency>
```

The application.properties file needs to be created in classpath. 
It should contain address to the zookeeper server.

zookeeper.connection.host - connection string to zookeeper server.<br/>

### Example
#### 1. Create application.properties with the following content
```properties
zookeeper.connection.host=127.0.0.1:2181
```

#### 2. Create interface and class. 

All custom objects must be serializable.
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

#### 3. Cretae service registrar
```java
ServiceRegistrar registrar = Network.newServiceRegistrar("service-name", "host:port", new ZookeeperDiscoveryConnector(), new NettyIoRmiConnectorServer());
```
`service-name` is name of your service<br/>
`host:port` is host and port of your service for example you can use
```java
 InetAddress.getLocalHost().getHostAddress()
```
to know your host address in network. To find free port
```java
private int freePort() {
        try {
            int port = 0;
            ServerSocket serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (IOException e) {
            return 0;
        }
    }
```

#### 4. Register service.
```java
registrar.register(new TestService(), "version1", "short service description");
```
To secure service just add token security handler
```java
registrar.register(new TestService(), "version1", "short service description", token -> token.equals("sometoken"));
```
so now zookeeper has record about ITestService, it includes interface name, host address and port.

#### 5. Create registrar client.

Client application has to have the same properties file with zookeeper host.
```java
ServiceRegistrarClient clientRegistrar = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector());
```

#### 6. Create service.
```java
ITestService testService = clientRegistrar.create(ITestService.class, "service-name", new NettyIoRmiConnectorClient());
```
To connect to secured service
```java
ITestService testService = clientRegistrar.create(ITestService.class, "service-name", new NettyIoRmiConnectorClient(), "sometoken");
```
`service-name` must be the same like in service registrar otherwise client is not gonna find service.
In case of service endpoint has changed host address all clients will be notified about that and will be configured automatically.

If several service hosts publish the same interface it's possible to collect list of interfaces
for example:
```java
List<ITestService> services = clientRegistrar.createForAll(ITestService.class, new NettyIoRmiConnectorClient());
```
