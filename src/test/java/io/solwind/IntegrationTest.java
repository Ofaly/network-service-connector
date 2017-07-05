package io.solwind;

import io.solwind.api.IDiscovery;
import io.solwind.api.IExposer;
import io.solwind.impl.Cluster;
import io.solwind.impl.NettyIoRmiConnectorClient;
import io.solwind.impl.NettyIoRmiConnectorServer;
import io.solwind.impl.ZookeeperDiscoveryConnector;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
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

    private IExposer exposer;

    private IExposer exposer1;

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
        exposer = Cluster.exposer("testExposerName", "localhost:8090", new ZookeeperDiscoveryConnector(properties), new NettyIoRmiConnectorServer());
        exposer1 = Cluster.exposer("testExposerName1", "localhost:8070", new ZookeeperDiscoveryConnector(properties), new NettyIoRmiConnectorServer());
        ITestService testServiceClass = new TestService();
        ITestService testServiceClass1 = new TestService1();
        exposer1.expose(testServiceClass1, "1", "Test description 2");
        exposer.expose(testServiceClass, "1", "Test description");
    }

    private void initPort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        zkPort = serverSocket.getLocalPort();
        serverSocket.close();
    }

    @Test
    public void test() throws IOException, InterruptedException {

        final IDiscovery discovery = Cluster.discovery(new ZookeeperDiscoveryConnector(properties));
        testService = discovery.lookup(ITestService.class, "testExposerName", new NettyIoRmiConnectorClient());

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

    }

    @Test
    public void testListOfInterfaces() throws IOException, InterruptedException {
        final IDiscovery discovery = Cluster.discovery(new ZookeeperDiscoveryConnector(properties));
        List<ITestService> services = discovery.lookupAll(ITestService.class, new NettyIoRmiConnectorClient());
        Assert.assertTrue(services.size() == 2);
        String[] result = {null, null};
        final int[] i = {0};
        services.forEach(iTestService -> {
            result[i[0]++] = iTestService.echoText();
        });
        Assert.assertTrue(checkResult.apply(result).apply(new String[]{"ok1", "ok"}));
    }

    @After
    public void destroy() throws InterruptedException {
//        zooKeeperServer.stop();
        exposer.stop();
        exposer1.stop();
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