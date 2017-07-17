package io.solwind.impl;

import io.solwind.api.DiscoveryConfig;
import io.solwind.api.ServiceRegistrar;
import io.solwind.api.RmiConnectorServer;
import io.solwind.api.TokenSecurityHandler;
import io.solwind.exception.DedicatedRuntimeException;
import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by solwind on 6/14/17.
 */
class Exposer implements ServiceRegistrar {

    public static final Logger LOGGER = LoggerFactory.getLogger(Exposer.class);

    private final String host;
    private final DiscoveryConfig discoveryConfig;
    private final RmiConnectorServer rmiConnectorServer;
    private final String exposerName;


    private Map<Class, Object> serviceTable = Collections.synchronizedMap(new HashMap<Class, Object>());
    private Map<Class, TokenSecurityHandler<Boolean>> handlerTable = Collections.synchronizedMap(new HashMap<Class, TokenSecurityHandler<Boolean>>());

    public Exposer(String exposerName, String host, DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        this.exposerName = exposerName;
        this.rmiConnectorServer = rmiConnectorServer;
        discoveryConfig.init();
        discoveryConfig.connect();
        this.host = host;
        this.discoveryConfig = discoveryConfig;
        String[] hostSplit = this.host.split(":");
        rmiConnectorServer.port(hostSplit.length > 1?new Integer(hostSplit[1]):freePort());
        rmiConnectorServer.serviceTable(serviceTable);
        rmiConnectorServer.handlerTable(handlerTable);
        new Thread(rmiConnectorServer).start();
    }

    public Exposer(DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        this.rmiConnectorServer = rmiConnectorServer;
        discoveryConfig.init();
        discoveryConfig.connect();
        this.host = discoveryConfig.props().getProperty("register.host");
        this.exposerName = discoveryConfig.props().getProperty("newServiceRegistrar.name");
        this.discoveryConfig = discoveryConfig;
        String[] hostSplit = this.host.split(":");
        rmiConnectorServer.port(hostSplit.length > 1?new Integer(hostSplit[1]):freePort());
        rmiConnectorServer.serviceTable(serviceTable);
        rmiConnectorServer.handlerTable(handlerTable);
        new Thread(rmiConnectorServer).start();
    }


    public <T> void register(T testServiceClass, String version, String shortDescription) throws KeeperException, InterruptedException {
        serviceTable.put(testServiceClass.getClass().getInterfaces()[0], testServiceClass);
        LOGGER.info("\nExpose for {}", testServiceClass);
        this.discoveryConfig.push(testServiceClass.getClass().getInterfaces()[0],
                new RegistrationServiceHolder(host, version, shortDescription, exposerName));
    }

    @Override
    public <T> void register(T testServiceClass, String version, String shortDescription, TokenSecurityHandler tokenSecurityHandler) throws KeeperException, InterruptedException {
        serviceTable.put(testServiceClass.getClass().getInterfaces()[0], testServiceClass);
        handlerTable.put(testServiceClass.getClass().getInterfaces()[0], tokenSecurityHandler);
        LOGGER.info("\nExpose for {}", testServiceClass);
        this.discoveryConfig.push(testServiceClass.getClass().getInterfaces()[0],
                new RegistrationServiceHolder(host, version, shortDescription, exposerName));
    }

    public void stop() throws InterruptedException {
        rmiConnectorServer.stop();
    }

    private int freePort() {
        try {
            int port = 0;
            ServerSocket serverSocket = new ServerSocket(0);
            port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (IOException e) {
            return 0;
        }
    }

}
