# Distributed service injector.

This library allows you to easy create communication between different services which run on different hosts. It uses the Apache zookeeper to find location of services, the netty io to connnect beatween services and java serialization to serialize queries beatween services.

The application.properties file needs to be created in classpath. It should contain address to the zookeeper server and port which will be used to call  services.

zookeeper.connection.host - connection string to zookeeper server.<br/>
expose.host - host to expose service.

### Example
1. application.properties
```properties
zookeeper.connection.host=127.0.0.1:2888
expose.host=127.0.0.1:8090
```

2. Service
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

3. Expose service
```java
IExposer exposer = Cluster.exposer(new ZookeeperDiscoveryConnector());
exposer.expose(new TestService(), "Some version", "Some description");
```

4. Consume service
To consume service need to have only <b>zookeeper.connection.host</b> into application properties file.

```java
ITestService iTestService = Cluster.discovery(new ZookeeperDiscoveryConnector()).lookup(ITestService.class);
```
