package io.solwind.impl;

import io.solwind.TestService;
import io.solwind.TestService1;
import io.solwind.api.DiscoveryConfig;
import io.solwind.api.ServiceRegistrar;
import io.solwind.api.RmiConnectorServer;
import org.apache.zookeeper.KeeperException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by theso on 7/1/2017.
 */
public class ExposerTest {

    @Mock
    private DiscoveryConfig discoveryConfig;

    @Mock
    private RmiConnectorServer rmiConnectorServer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void exposeWithPassedHostArgument() throws Exception {
        ServiceRegistrar serviceRegistrar = new Exposer("testexposername", "host:8080", discoveryConfig, rmiConnectorServer);
        serviceRegistrar.register(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void expose() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("register.host", "host:8080");
        properties.setProperty("serviceRegistrar.name", "testExposerName");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        ServiceRegistrar serviceRegistrar = new Exposer(discoveryConfig, rmiConnectorServer);
        serviceRegistrar.register(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig, Mockito.times(2)).props();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void exposeWithPassedHostArgumentWithDefaultPort() throws Exception {
        ServiceRegistrar serviceRegistrar = new Exposer("testexposername", "host", discoveryConfig, rmiConnectorServer);
        serviceRegistrar.register(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void exposeWithDefaultPort() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("register.host", "host");
        properties.setProperty("serviceRegistrar.name", "testExposerName");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        ServiceRegistrar serviceRegistrar = new Exposer(discoveryConfig, rmiConnectorServer);
        serviceRegistrar.register(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig, Mockito.times(2)).props();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void stop() throws Exception {
        ServiceRegistrar serviceRegistrar = new Exposer("testexposername", "host:8080", discoveryConfig, rmiConnectorServer);
        serviceRegistrar.register(new TestService(), "0", "description");
        serviceRegistrar.stop();
    }

    @Test
    public void exposeMoreThenOneServicesByOneExposer() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("register.host", "host:8080");
        properties.setProperty("serviceRegistrar.name", "testExposerName");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        ServiceRegistrar serviceRegistrar = new Exposer(discoveryConfig, rmiConnectorServer);

        serviceRegistrar.register(new TestService(), "0", "description");
        serviceRegistrar.register(new TestService1(), "0", "description");

        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig, Mockito.times(2)).props();
        Mockito.verify(discoveryConfig, Mockito.times(2)).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void exposeServiceWithTokenHandler() throws IOException, InterruptedException, KeeperException {
        Properties properties = new Properties();
        properties.setProperty("register.host", "host:8080");
        properties.setProperty("serviceRegistrar.name", "testExposerName");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        ServiceRegistrar serviceRegistrar = new Exposer(discoveryConfig, rmiConnectorServer);
        serviceRegistrar.register(new TestService(), "0", "description", token -> token.equals("1234567890"));
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig, Mockito.times(2)).props();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

}