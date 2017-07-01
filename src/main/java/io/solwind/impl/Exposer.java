package io.solwind.impl;

import io.solwind.api.DiscoveryConfig;
import io.solwind.api.IExposer;
import io.solwind.api.RmiConnectorServer;
import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by solwind on 6/14/17.
 */
class Exposer implements IExposer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Exposer.class);

    private final String host;
    private final DiscoveryConfig discoveryConfig;
    private final RmiConnectorServer rmiConnectorServer;


    private Map<Class, Object> serviceTable = Collections.synchronizedMap(new HashMap<Class, Object>());

    public Exposer(String host, DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        this.rmiConnectorServer = rmiConnectorServer;
        discoveryConfig.init();
        discoveryConfig.connect();
        this.host = host;
        this.discoveryConfig = discoveryConfig;
        String[] hostSplit = this.host.split(":");
        rmiConnectorServer.port(hostSplit.length > 1?new Integer(hostSplit[1]):80);
        rmiConnectorServer.serviceTable(serviceTable);
        new Thread(rmiConnectorServer).start();
    }

    public Exposer(DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        this.rmiConnectorServer = rmiConnectorServer;
        discoveryConfig.init();
        discoveryConfig.connect();
        this.host = discoveryConfig.props().getProperty("expose.host");
        this.discoveryConfig = discoveryConfig;
        String[] hostSplit = this.host.split(":");
        rmiConnectorServer.port(hostSplit.length > 1?new Integer(hostSplit[1]):80);
        rmiConnectorServer.serviceTable(serviceTable);
        new Thread(rmiConnectorServer).start();
    }


    public <T> void expose(T testServiceClass, String version, String shortDescription) throws KeeperException, InterruptedException {
        serviceTable.put(testServiceClass.getClass().getInterfaces()[0], testServiceClass);
        LOGGER.info("\nExpose for {}", testServiceClass);
        this.discoveryConfig.push(testServiceClass.getClass().getInterfaces()[0].getCanonicalName(), new RegistrationServiceHolder(host, version, shortDescription));
    }

    public void stop() throws InterruptedException {
        rmiConnectorServer.stop();
    }
}
