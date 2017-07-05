package io.solwind.impl;

import io.solwind.Functions;
import io.solwind.api.*;
import io.solwind.handler.MethodInvocationHandler;
import io.solwind.handler.RegistrationServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by solwind on 6/14/17.
 */
public final class Cluster implements IInjector {

    public static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private Cluster() {
    }

    public static IExposer exposer(String exposerName, String host, DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        return new Exposer(exposerName, host, discoveryConfig, rmiConnectorServer);
    }

    public static IExposer exposer(DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        return new Exposer(discoveryConfig, rmiConnectorServer);
    }

    public static IDiscovery discovery(final DiscoveryConfig discoveryConfig) {

        return new IDiscovery() {
            public <T> T lookup(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException {
                discoveryConfig.init();
                discoveryConfig.connect();
                String concat = "/".concat(service.getCanonicalName());
                RegistrationServiceHolder znode = Functions.searchRshByName.apply(discoveryConfig.retrieveAll(concat)).apply(exposerName);
                LOGGER.info("\nPrepare to create proxy for {}, {}", service, znode);
                MethodInvocationHandler h = new MethodInvocationHandler();
                String host = znode.getHost().split(":")[0];
                Integer port = Integer.valueOf(znode.getHost().split(":")[1]);
                rmiConnectorClient.setHost(host);
                rmiConnectorClient.setPort(port);
                rmiConnectorClient.reconnect();
                h.setRmiConnectorClient(rmiConnectorClient);
                return (T) Proxy.newProxyInstance(IDiscovery.class.getClassLoader(),
                        new Class<?>[]{service}, h);
            }

            @Override
            public <T> List<T> lookupAll(Class<T> service, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException {
                String concat = "/".concat(service.getCanonicalName());
                discoveryConfig.init();
                discoveryConfig.connect();
                List<RegistrationServiceHolder> holders = discoveryConfig.retrieveAll(concat);
                List<T> ts = new ArrayList<>();
                for (RegistrationServiceHolder holder : holders) {
                    LOGGER.info("\nPrepare to create proxy for {}, {}", service, holder);
                    MethodInvocationHandler h = new MethodInvocationHandler();
                    String host = holder.getHost().split(":")[0];
                    Integer port = Integer.valueOf(holder.getHost().split(":")[1]);
                    rmiConnectorClient.setHost(host);
                    rmiConnectorClient.setPort(port);
                    rmiConnectorClient.reconnect();
                    h.setRmiConnectorClient(rmiConnectorClient);
                    ts.add((T) Proxy.newProxyInstance(IDiscovery.class.getClassLoader(),
                            new Class<?>[]{service}, h));
                }
                return ts;
            }
        };
    }
}
