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
import java.util.Set;

/**
 * Created by solwind on 6/14/17.
 */
public final class Network implements IInjector {

    public static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
    public static final String PREPARE_TO_CREATE_PROXY_MESSAGE = "\nPrepare to create proxy for {}, {}";

    private Network() {
    }

    public static ServiceRegistrar newServiceRegistrar(String exposerName, String host, DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        return new Exposer(exposerName, host, discoveryConfig, rmiConnectorServer);
    }

    public static ServiceRegistrar newServiceRegistrar(DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) throws IOException, InterruptedException {
        return new Exposer(discoveryConfig, rmiConnectorServer);
    }

    public static ServiceRegistrarClient newServiceRegistrarClient(final DiscoveryConfig discoveryConfig) {

        return new ServiceRegistrarClient() {
            public <T> T create(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException {
                discoveryConfig.init();
                discoveryConfig.connect();
                String concat = "/".concat(service.getCanonicalName());
                RegistrationServiceHolder znode = Functions.searchRshByName.apply(discoveryConfig.retrieveAll(concat)).apply(exposerName);
                LOGGER.info(PREPARE_TO_CREATE_PROXY_MESSAGE, service, znode);
                MethodInvocationHandler h = new MethodInvocationHandler();
                String host = znode.getHost().split(":")[0];
                Integer port = Integer.valueOf(znode.getHost().split(":")[1]);
                h.setRmiConnectorClient(rmiConnectorClient.newClient(host, port));
                return (T) Proxy.newProxyInstance(ServiceRegistrarClient.class.getClassLoader(),
                        new Class<?>[]{service}, h);
            }

            @Override
            public <T> T create(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient, String token) throws IOException, InterruptedException {
                discoveryConfig.init();
                discoveryConfig.connect();
                String concat = "/".concat(service.getCanonicalName());
                RegistrationServiceHolder znode = Functions.searchRshByName.apply(discoveryConfig.retrieveAll(concat)).apply(exposerName);
                LOGGER.info(PREPARE_TO_CREATE_PROXY_MESSAGE, service, znode);
                MethodInvocationHandler h = new MethodInvocationHandler();
                String host = znode.getHost().split(":")[0];
                Integer port = Integer.valueOf(znode.getHost().split(":")[1]);
                h.setRmiConnectorClient(rmiConnectorClient.newClient(host, port));
                h.setToken(token);
                return (T) Proxy.newProxyInstance(ServiceRegistrarClient.class.getClassLoader(),
                        new Class<?>[]{service}, h);
            }

            @Override
            public <T> List<T> createForAll(Class<T> service, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException {
                String concat = "/".concat(service.getCanonicalName());
                discoveryConfig.init();
                discoveryConfig.connect();
                Set<RegistrationServiceHolder> holders = discoveryConfig.retrieveAll(concat);
                List<T> ts = new ArrayList<>();
                for (RegistrationServiceHolder holder : holders) {
                    LOGGER.info(PREPARE_TO_CREATE_PROXY_MESSAGE, service, holder);
                    MethodInvocationHandler h = new MethodInvocationHandler();
                    String host = holder.getHost().split(":")[0];
                    Integer port = Integer.valueOf(holder.getHost().split(":")[1]);
                    h.setRmiConnectorClient(rmiConnectorClient.newClient(host, port));
                    ts.add((T) Proxy.newProxyInstance(ServiceRegistrarClient.class.getClassLoader(),
                            new Class<?>[]{service}, h));
                }
                return ts;
            }
        };
    }
}
