package io.solwind.impl;

import io.solwind.ITestService;
import io.solwind.api.DiscoveryConfig;
import io.solwind.api.RmiConnectorClient;
import io.solwind.api.RmiConnectorServer;
import io.solwind.handler.RegistrationServiceHolder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Created by theso on 7/1/2017.
 */
public class ClusterTest {

    @Mock
    private DiscoveryConfig discoveryConfig;

    @Mock
    private RmiConnectorServer rmiConnectorServer;

    @Mock
    private RmiConnectorClient rmiConnectorClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void discovery() throws Exception {
        Mockito.when(discoveryConfig.retrieve("/".concat(ITestService.class.getCanonicalName())))
                .thenReturn(new RegistrationServiceHolder("host:8080", "0", "some desc"));
        Cluster.connectorServer = rmiConnectorServer;
        Cluster.rmiConnectorClient = rmiConnectorClient;
        ITestService lookup = Cluster.discovery(discoveryConfig).lookup(ITestService.class);
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(rmiConnectorClient).setPort(8080);
        Mockito.verify(rmiConnectorClient).setHost("host");
        Mockito.verify(rmiConnectorClient).reconnect();
    }

    @Test
    public void exposer() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("expose.host", "host");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        Cluster.exposer(discoveryConfig);
    }

    @Test
    public void exposerWithHost() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("expose.host", "host");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        Cluster.exposer("host:8080", discoveryConfig);
    }

}