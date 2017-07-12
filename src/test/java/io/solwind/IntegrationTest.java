package io.solwind;

import io.solwind.api.ServiceRegistrarClient;
import io.solwind.api.ServiceRegistrar;
import io.solwind.impl.Network;
import io.solwind.impl.NettyIoRmiConnectorClient;
import io.solwind.impl.NettyIoRmiConnectorServer;
import io.solwind.impl.ZookeeperDiscoveryConnector;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

/**
 * Created by solwind on 6/14/17.
 */
@RunWith(JUnit4.class)
@Ignore
public class IntegrationTest {

    private ServiceRegistrar exposer;

    private ServiceRegistrar exposer1;

    private ServiceRegistrar exposer3;

    private ITestService testService;

    final Properties properties = new Properties();

    private CustomZooKeeperServerMain zooKeeperServer;

    private int zkPort;

    private static final Function<String[], Function<String[], Boolean>> checkResult = strings -> s -> {
        String[] array1 = Arrays.copyOf(strings, strings.length);
        String[] array2 = Arrays.copyOf(s, strings.length);
        Arrays.sort(array1);
        Arrays.sort(array2);
        return Arrays.equals(array1,(array2));
    };

    @Before
    public void init() throws IOException, InterruptedException, KeeperException {

        initPort();

        embeddedZookeeperServer();

        properties.setProperty("zookeeper.connection.host", "localhost:" + zkPort);
        exposer = Network.newServiceRegistrar("testExposerName", "localhost:8090", new ZookeeperDiscoveryConnector(properties), new NettyIoRmiConnectorServer());
        exposer1 = Network.newServiceRegistrar("testExposerName1", "localhost:8070", new ZookeeperDiscoveryConnector(properties), new NettyIoRmiConnectorServer());
        exposer3 = Network.newServiceRegistrar("testExposerName3", "localhost:8071", new ZookeeperDiscoveryConnector(properties), new NettyIoRmiConnectorServer());
        ITestService testServiceClass = new TestService();
        ITestService testServiceClass1 = new TestService1();
        ITestService3 testService3 = new TestService3();
        exposer1.register(testServiceClass1, "1", "Test description 2");
        exposer.register(testServiceClass, "1", "Test description");
        exposer.register(testService3, "1", "Test description");
        exposer3.register(testService3, "1", "Test description", token -> token.equals("000000"));
    }

    private void initPort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        zkPort = serverSocket.getLocalPort();
        serverSocket.close();
    }

    @Test
    public void test() throws IOException, InterruptedException {

        final ServiceRegistrarClient discovery = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties));
        testService = discovery.create(ITestService.class, "testExposerName", new NettyIoRmiConnectorClient());

        String responseText = testService.echoText();
        Assert.assertEquals("ok", responseText);

        Integer responseInt = testService.echoInt();
        Assert.assertEquals(new Integer(123), responseInt);

        CustomDto responseCustom = testService.customDto();
        CustomDto customDto = new CustomDto("testOk", 123456);

        Assert.assertTrue(customDto.equals(responseCustom));
        Assert.assertFalse(customDto == responseCustom);

        String responseArgStr = testService.args("test request");
        Assert.assertEquals("response for arg test request", responseArgStr);

        List<CustomDto> list = testService.list();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() == 10001);

        for (int i = 0; i<=1000; i++) {
            Integer integer = testService.echoInt();
            Assert.assertNotNull(integer);
        }

    }

//    @Test
//    public void passNullValueParameters() throws IOException, InterruptedException {
//        final ServiceRegistrarClient discovery = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties));
//        testService = discovery.create(ITestService.class, "testExposerName", new NettyIoRmiConnectorClient());
//
//        String args = testService.args(null);
//
//        Assert.assertEquals("response for arg null", args);
//
//    }


    @Test
    public void testListOfInterfaces() throws IOException, InterruptedException {
        final ServiceRegistrarClient discovery = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties));
        List<ITestService> services = discovery.createForAll(ITestService.class, new NettyIoRmiConnectorClient());
        Assert.assertTrue(services.size() == 2);
        String[] result = {null, null};
        final int[] i = {0};
        services.forEach(iTestService -> result[i[0]++] = iTestService.echoText());
        Assert.assertTrue(checkResult.apply(result).apply(new String[]{"ok1", "ok"}));
    }

    @Test
    public void exposeMoreThenOneServicesByOneExposer() throws IOException, InterruptedException {
        ITestService testExposerName = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties)).create(ITestService.class, "testExposerName", new NettyIoRmiConnectorClient());
        ITestService3 testExposerName2 = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties)).create(ITestService3.class, "testExposerName", new NettyIoRmiConnectorClient());

        Assert.assertEquals("ok", testExposerName.echoText());
        Assert.assertEquals("ok3", testExposerName2.test());

    }

    @Test
    public void lookupWithSecurity() throws IOException, InterruptedException {
        ServiceRegistrarClient discovery = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties));
        ITestService3 testExposerName3 = discovery.create(ITestService3.class, "testExposerName3", new NettyIoRmiConnectorClient(), "000000");
        Assert.assertEquals("ok3", testExposerName3.test());
    }

    @Test
    public void lookupWithWrongSecurity() throws IOException, InterruptedException {
        ServiceRegistrarClient discovery = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties));
        ITestService3 testExposerName3 = discovery.create(ITestService3.class, "testExposerName3", new NettyIoRmiConnectorClient(), "0000000");
        Assert.assertNull(testExposerName3.test());
    }

    @Test
    public void lookupWithNullSecurity() throws IOException, InterruptedException {
        ServiceRegistrarClient discovery = Network.newServiceRegistrarClient(new ZookeeperDiscoveryConnector(properties));
        ITestService3 testExposerName3 = discovery.create(ITestService3.class, "testExposerName3", new NettyIoRmiConnectorClient());
        Assert.assertNull(testExposerName3.test());
    }

    @After
    public void destroy() throws InterruptedException {
//        zooKeeperServer.stop();
        exposer.stop();
        exposer1.stop();
        exposer3.stop();
    }

    private void embeddedZookeeperServer() {
        Properties startupProperties = new Properties();
        startupProperties.put("dataDir", "./target/tmp");
        startupProperties.put("clientPort", zkPort);

        QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
        try {
            quorumConfiguration.parseProperties(startupProperties);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        zooKeeperServer = new CustomZooKeeperServerMain();
        final ServerConfig configuration = new ServerConfig();
        configuration.readFrom(quorumConfiguration);

        new Thread(() -> {
            try {
                zooKeeperServer.runFromConfig(configuration);
            } catch (IOException e) {
                System.out.println(e);
            }
        }).start();
    }

}