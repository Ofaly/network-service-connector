package io.solwind.impl;

import io.solwind.api.*;
import io.solwind.handler.MethodInvocationHandler;
import io.solwind.handler.RegistrationServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;

/**
 * Created by solwind on 6/14/17.
 */
public final class Cluster implements IInjector {

    public static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    static RmiConnectorServer connectorServer = new NettyIoRmiConnectorServer();

    static RmiConnectorClient rmiConnectorClient = new NettyIoRmiConnectorClient();

    private Cluster() {
    }

    public static IExposer exposer(String host, DiscoveryConfig discoveryConfig) throws IOException, InterruptedException {
        return new Exposer(host, discoveryConfig, connectorServer);
    }

    public static IExposer exposer(DiscoveryConfig discoveryConfig) throws IOException, InterruptedException {
        return new Exposer(discoveryConfig, connectorServer);
    }

    public static IDiscovery discovery(final DiscoveryConfig discoveryConfig) {

        return new IDiscovery() {
            public <T> T lookup(Class<T> service) throws IOException, InterruptedException {
                discoveryConfig.init();
                discoveryConfig.connect();
                String concat = "/".concat(service.getCanonicalName());
                RegistrationServiceHolder znode = discoveryConfig.retrieve(concat);
                LOGGER.info("\nPrepare to create proxy for {}, {}",service,
                        String.format("\nHost: %s \nVersion: %s \nDescription: %s", znode.getHost(),
                                znode.getVersion(), znode.getShortDescription()));
                MethodInvocationHandler h = new MethodInvocationHandler(znode);
                String host = znode.getHost().split(":")[0];
                Integer port = Integer.valueOf(znode.getHost().split(":")[1]);
                rmiConnectorClient.setHost(host);
                rmiConnectorClient.setPort(port);
                rmiConnectorClient.reconnect();
                h.setRmiConnectorClient(rmiConnectorClient);
                return (T) Proxy.newProxyInstance(IDiscovery.class.getClassLoader(),
                        new Class<?>[]{service}, h);
            }
        };
    }
}
