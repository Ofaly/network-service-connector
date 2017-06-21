package org.solwind;

import org.apache.zookeeper.KeeperException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by solwind on 6/14/17.
 */
@RunWith(JUnit4.class)
public class InjectorTest {

    private IExposer exposer;

    private ITestService testService;

    final Properties properties = new Properties();

    @Before
    public void init() throws IOException, InterruptedException, KeeperException {
        properties.setProperty("host", "localhost");
        exposer = Cluster.exposer(8090, new ZookeeperDiscoveryConnector(properties));
        ITestService testServiceClass = new TestService();
        exposer.expose(testServiceClass);
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

}