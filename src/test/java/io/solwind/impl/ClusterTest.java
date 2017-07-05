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

import java.io.IOException;
import java.util.*;

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
        Set<RegistrationServiceHolder> holders = new HashSet<>();
        holders.add(new RegistrationServiceHolder("host:8080", "0", "some desc", "exposerName"));
        Mockito.when(discoveryConfig.retrieveAll("/".concat(ITestService.class.getCanonicalName())))
                .thenReturn(holders);
        ITestService lookup = Cluster.discovery(discoveryConfig).lookup(ITestService.class, "exposerName", rmiConnectorClient);
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(rmiConnectorClient).setPort(8080);
        Mockito.verify(rmiConnectorClient).setHost("host");
        Mockito.verify(rmiConnectorClient).reconnect();
    }

    @Test
    public void lookUpList() throws IOException, InterruptedException {

        Set<RegistrationServiceHolder> holders = new HashSet<>();
        holders.add(new RegistrationServiceHolder("host:8080", "0", "some desc 1", "exposerOne"));
        holders.add(new RegistrationServiceHolder("host1:8081", "0", "some desc 2", "exposerTwo"));

        Mockito.when(discoveryConfig.retrieveAll("/" + ITestService.class.getCanonicalName()))
                .thenReturn(holders);

        List<ITestService> services = Cluster.discovery(discoveryConfig).lookupAll(ITestService.class, rmiConnectorClient);
        assertTrue(services.size() == 2);
    }

    @Test
    public void exposer() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("expose.host", "host");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        Cluster.exposer(discoveryConfig, rmiConnectorServer);
    }

    @Test
    public void exposerWithHost() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("expose.host", "host");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        Cluster.exposer("testExposer", "host:8080", discoveryConfig, rmiConnectorServer);
    }

}