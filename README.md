[![Build Status](https://travis-ci.org/thesolwind/distributed-service-injector.svg?branch=master)](https://travis-ci.org/thesolwind/distributed-service-injector)
# Distributed service injector.[DRAFT]

This library allows you to easy create communication between different services which run on different hosts. It uses the Apache zookeeper to find location of services, the netty io to connnect beatween services and java serialization to serialize queries beatween services.

The application.properties file needs to be created in classpath. It should contain address to the zookeeper server and port which will be used to call  services.

zookeeper.connection.host - connection string to zookeeper server.<br/>
expose.host - host to expose service.

### Example
1. Create application.properties with the following content
```properties
zookeeper.connection.host=127.0.0.1:2888
expose.host=127.0.0.1:8090
```

2. To expose a service create interface and class. All custom objects must be serializable.
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

3. Expose service. It's gonna be listen 8090 port of your host to consume inbound calls.
```java
IExposer exposer = Cluster.exposer(new ZookeeperDiscoveryConnector());
exposer.expose(new TestService(), "Some version", "Some description");
```
After this point zookeeper will have the record about exposed service which looks like: path = /[your.package].ITestService, data = 
<code>{'host':'127.0.0.1:8090', 'version':'Some version', 'description':'Some description'}</code>


4. To consume service from an another instance of JVM need to have only <b>zookeeper.connection.host</b> into application properties file and consuming interface.

```java
ITestService iTestService = Cluster.discovery(new ZookeeperDiscoveryConnector()).lookup(ITestService.class);
```
