package io.solwind.impl;

import io.solwind.ITestService;
import io.solwind.api.DiscoveryConfig;
import io.solwind.api.RmiConnectorClient;
import io.solwind.api.RmiConnectorServer;
import io.solwind.handler.MethodInvocationHandler;
import io.solwind.handler.RegistrationServiceHolder;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * Created by theso on 7/1/2017.
 */
public class NetworkTest {

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
        Mockito.when(discoveryConfig.retrieveAll(Matchers.eq("/".concat(ITestService.class.getCanonicalName())), Matchers.any()))
                .thenReturn(holders);
        ITestService lookup = Network.newServiceRegistrarClient(discoveryConfig).create(ITestService.class, "exposerName", rmiConnectorClient);
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(rmiConnectorClient).newClient("host", 8080);
    }

    @Test
    public void lookupWithToken() throws Exception {
        Set<RegistrationServiceHolder> holders = new HashSet<>();
        holders.add(new RegistrationServiceHolder("host:8080", "0", "some desc", "exposerName"));
        Mockito.when(discoveryConfig.retrieveAll(Matchers.eq("/".concat(ITestService.class.getCanonicalName())), Matchers.any()))
                .thenReturn(holders);
        ITestService lookup = Network.newServiceRegistrarClient(discoveryConfig).create(ITestService.class, "exposerName", rmiConnectorClient, "123456");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(rmiConnectorClient).newClient("host", 8080);
    }

    @Test
    public void lookUpList() throws IOException, InterruptedException {

        Set<RegistrationServiceHolder> holders = new HashSet<>();
        holders.add(new RegistrationServiceHolder("host:8080", "0", "some desc 1", "exposerOne"));
        holders.add(new RegistrationServiceHolder("host1:8081", "0", "some desc 2", "exposerTwo"));

        Mockito.when(discoveryConfig.retrieveAll(Matchers.eq("/" + ITestService.class.getCanonicalName()), Matchers.any()))
                .thenReturn(holders);

        List<ITestService> services = Network.newServiceRegistrarClient(discoveryConfig).createForAll(ITestService.class, rmiConnectorClient);
        assertTrue(services.size() == 2);
    }

    @Test
    public void exposer() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("register.host", "host");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        Network.newServiceRegistrar(discoveryConfig, rmiConnectorServer);
    }

    @Test
    public void exposerWithHost() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("register.host", "host");
        Mockito.when(discoveryConfig.props()).thenReturn(properties);
        Network.newServiceRegistrar("testExposer", "host:8080", discoveryConfig, rmiConnectorServer);
    }

}