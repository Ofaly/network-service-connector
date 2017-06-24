package org.solwind;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

/**
 * Created by solwind on 6/14/17.
 */
@RunWith(JUnit4.class)
public class IntegrationTest {

    private IExposer exposer;

    private ITestService testService;

    final Properties properties = new Properties();

    private ZooKeeperServerMain zooKeeperServer;

    private int zkPort;

    @Before
    public void init() throws IOException, InterruptedException, KeeperException {

        initPort();

        embeddedZookeeperServer();

        properties.setProperty("zookeeper.connection.host", "localhost:" + zkPort);
        exposer = Cluster.exposer("localhost:8090", new ZookeeperDiscoveryConnector(properties));
        ITestService testServiceClass = new TestService();
        exposer.expose(testServiceClass);
    }

    private void initPort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        zkPort = serverSocket.getLocalPort();
        serverSocket.close();
    }

    @Test()
    public void test() throws IOException, InterruptedException {

        final IDiscovery discovery = Cluster.discovery(new ZookeeperDiscoveryConnector(properties));
        testService = discovery.lookup(ITestService.class);

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

        zooKeeperServer = new ZooKeeperServerMain();
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