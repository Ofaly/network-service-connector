package io.solwind.handler;

import io.solwind.TestService;
import io.solwind.api.RmiConnectorClient;
import io.solwind.protocol.CallResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by theso on 6/30/2017.
 */
public class MethodInvocationHandlerTest {

    private MethodInvocationHandler methodInvocationHandler;

    @Mock
    private RmiConnectorClient rmiConnectorClient;

    @Before
    public void setup() throws InterruptedException {

        MockitoAnnotations.initMocks(this);

        methodInvocationHandler = new MethodInvocationHandler();
        methodInvocationHandler.setRmiConnectorClient(rmiConnectorClient);
    }

    @Test
    public void invoke() throws Throwable {
        TestService proxy = new TestService();
        Mockito.when(rmiConnectorClient.lastResponse()).thenReturn(new CallResponse("ok"));
        Object echoText = methodInvocationHandler.invoke(proxy, proxy.getClass().getMethod("echoText", null), null);
        assertEquals("ok", echoText);
        Mockito.verify(rmiConnectorClient).writeAndFlush(Matchers.any());
        Mockito.verify(rmiConnectorClient).lastResponse();
        Mockito.verify(rmiConnectorClient).waitForResponse();
    }

    @Test
    public void invokeNull() throws Throwable {
        TestService proxy = new TestService();
        Mockito.when(rmiConnectorClient.lastResponse()).thenReturn(null);
        Object echoText = methodInvocationHandler.invoke(proxy, proxy.getClass().getMethod("echoText", null), null);
        assertNull(echoText);
        Mockito.verify(rmiConnectorClient).writeAndFlush(Matchers.any());
        Mockito.verify(rmiConnectorClient).lastResponse();
        Mockito.verify(rmiConnectorClient).waitForResponse();
    }

    @Test
    public void acceptChangeHostAndPortTest() {
        Set<RegistrationServiceHolder> set = new HashSet<>();
        set.add(new RegistrationServiceHolder("host:88", "1", "description", "testName"));
        set.add(new RegistrationServiceHolder("host:89", "1", "description", "testName1"));
        set.add(new RegistrationServiceHolder("host:90", "1", "description", "testName2"));

        methodInvocationHandler.setExposerName("testName");
        methodInvocationHandler.setRmiConnectorClient(rmiConnectorClient);
        ((Consumer)methodInvocationHandler).accept(set);

        Mockito.verify(rmiConnectorClient).setPort(88);
        Mockito.verify(rmiConnectorClient).setHost("host");
    }

    @Test
    public void acceptChangeWrongHostAndPortTest() {
        Set<RegistrationServiceHolder> set = new HashSet<>();
        set.add(new RegistrationServiceHolder("host:88", "1", "description", "testName"));
        set.add(new RegistrationServiceHolder("host:89", "1", "description", "testName1"));
        set.add(new RegistrationServiceHolder("host:90", "1", "description", "testName2"));

        methodInvocationHandler.setExposerName("testName4");
        methodInvocationHandler.setRmiConnectorClient(rmiConnectorClient);
        ((Consumer)methodInvocationHandler).accept(set);

        Mockito.verifyZeroInteractions(rmiConnectorClient);
        Mockito.verifyZeroInteractions(rmiConnectorClient);
    }

}