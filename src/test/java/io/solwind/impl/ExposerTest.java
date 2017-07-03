package io.solwind.impl;

import io.solwind.TestService;
import io.solwind.api.DiscoveryConfig;
import io.solwind.api.IExposer;
import io.solwind.api.RmiConnectorServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.*;

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
        IExposer iExposer = new Exposer("testexposername", "host:8080", discoveryConfig, rmiConnectorServer);
        iExposer.expose(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void expose() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("expose.host", "host:8080");
        properties.setProperty("exposer.name", "testExposerName");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        IExposer iExposer = new Exposer(discoveryConfig, rmiConnectorServer);
        iExposer.expose(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig, Mockito.times(2)).props();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void exposeWithPassedHostArgumentWithDefaultPort() throws Exception {
        IExposer iExposer = new Exposer("testexposername","host", discoveryConfig, rmiConnectorServer);
        iExposer.expose(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void exposeWithDefaultPort() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("expose.host", "host");
        properties.setProperty("exposer.name", "testExposerName");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        IExposer iExposer = new Exposer(discoveryConfig, rmiConnectorServer);
        iExposer.expose(new TestService(), "0", "description");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(discoveryConfig, Mockito.times(2)).props();
        Mockito.verify(discoveryConfig).push(Matchers.any(), Matchers.any());
    }

    @Test
    public void stop() throws Exception {
        IExposer iExposer = new Exposer("testexposername", "host:8080", discoveryConfig, rmiConnectorServer);
        iExposer.expose(new TestService(), "0", "description");
        iExposer.stop();
    }

}