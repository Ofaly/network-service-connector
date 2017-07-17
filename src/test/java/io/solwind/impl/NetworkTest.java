package io.solwind.impl;

import io.solwind.ITestService;
import io.solwind.api.DiscoveryConfig;
import io.solwind.api.RmiConnectorClient;
import io.solwind.api.RmiConnectorServer;
import io.solwind.exception.DedicatedRuntimeException;
import io.solwind.handler.MethodInvocationHandler;
import io.solwind.handler.RegistrationServiceHolder;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Ignore;
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

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest1() throws IOException, InterruptedException {
        Network.newServiceRegistrar(null, null, null, null);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest2() throws IOException, InterruptedException {
        Network.newServiceRegistrar(null, null, null, rmiConnectorServer);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest3() throws IOException, InterruptedException {
        Network.newServiceRegistrar(null, null, discoveryConfig, rmiConnectorServer);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest4() throws IOException, InterruptedException {
        Network.newServiceRegistrar(null, "somehost", discoveryConfig, rmiConnectorServer);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest5() throws IOException, InterruptedException {
        Network.newServiceRegistrar(null, "", discoveryConfig, rmiConnectorServer);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest6() throws IOException, InterruptedException {
        Network.newServiceRegistrar("somename", "somehost", discoveryConfig, rmiConnectorServer);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest7() throws IOException, InterruptedException {
        Network.newServiceRegistrar("", "somehost", discoveryConfig, rmiConnectorServer);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest8() throws IOException, InterruptedException {
        Mockito.when(discoveryConfig.props()).thenReturn(new Properties());
        Network.newServiceRegistrar("", "somehost", discoveryConfig, rmiConnectorServer);
    }

    @Test(expected = DedicatedRuntimeException.class)
    public void wrongConfigTest9() throws IOException, InterruptedException {
        Mockito.when(discoveryConfig.props()).thenReturn(new Properties());
        Network.newServiceRegistrar("test-name", "somehost", discoveryConfig, rmiConnectorServer);
    }

    @Test
    public void wrongConfigTest10() throws IOException, InterruptedException {
        Properties t = new Properties();
        t.setProperty("zookeeper.connection.host", "localhost");
        Mockito.when(discoveryConfig.check()).thenReturn(true);
        Network.newServiceRegistrar("test-name", "somehost", discoveryConfig, rmiConnectorServer);
    }


    @Test
    public void discovery() throws Exception {
        Set<RegistrationServiceHolder> holders = new HashSet<>();
        holders.add(new RegistrationServiceHolder("host:8080", "0", "some desc", "exposerName"));
        Mockito.when(discoveryConfig.retrieveAll(Matchers.eq("/".concat(ITestService.class.getCanonicalName())),
                Matchers.any(), Matchers.eq("exposerName")))
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
        Mockito.when(discoveryConfig.retrieveAll(Matchers.eq("/".concat(ITestService.class.getCanonicalName())), Matchers.any(),
                Matchers.eq("exposerName")))
                .thenReturn(holders);
        ITestService lookup = Network.newServiceRegistrarClient(discoveryConfig).create(ITestService.class, "exposerName", rmiConnectorClient, "123456");
        Mockito.verify(discoveryConfig).init();
        Mockito.verify(discoveryConfig).connect();
        Mockito.verify(rmiConnectorClient).newClient("host", 8080);
    }

    @Test
    public void lookUpList() throws IOException, InterruptedException {

        Set<RegistrationServiceHolder> holders = new HashSet<>();
        RegistrationServiceHolder exposerOne = new RegistrationServiceHolder("host:8080", "0", "some desc 1", "exposerOne");
        holders.add(exposerOne);
        RegistrationServiceHolder exposerTwo = new RegistrationServiceHolder("host1:8081", "0", "some desc 2", "exposerTwo");
        holders.add(exposerTwo);

        Mockito.when(discoveryConfig.retrieveAll(Matchers.eq("/" + ITestService.class.getCanonicalName()), Matchers.any(), Matchers.anyString()))
                .thenReturn(holders);

        Mockito.when(discoveryConfig.retrieveAll("/" + ITestService.class.getCanonicalName(), null))
                .thenReturn(holders);

        Mockito.when(discoveryConfig.retrieveAll("/" + ITestService.class.getCanonicalName(), null))
                .thenReturn(holders);

        List<ITestService> services = Network.newServiceRegistrarClient(discoveryConfig).createForAll(ITestService.class, rmiConnectorClient);
        assertTrue(services.size() == 2);
    }

}