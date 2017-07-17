package io.solwind.impl;

import io.solwind.Functions;
import io.solwind.api.*;
import io.solwind.exception.DedicatedRuntimeException;
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
        checkInputParametersToCreateServiceRegistrar(exposerName, host, discoveryConfig, rmiConnectorServer);
        return new Exposer(exposerName, host, discoveryConfig, rmiConnectorServer);
    }

    private static void checkInputParametersToCreateServiceRegistrar(String exposerName, String host, DiscoveryConfig discoveryConfig, RmiConnectorServer rmiConnectorServer) {
        if (discoveryConfig == null || rmiConnectorServer == null || !discoveryConfig.check()
                || exposerName == null || exposerName.isEmpty() || host == null || host.isEmpty()) {
            throw new DedicatedRuntimeException(String.format("Input params are wrong!!!!\nCurrent settings: discoveryConfig - %s, rmiConnectorServer - %s" +
                    " discoveryConfig check result - %s, exposerName - %s, host - %s", discoveryConfig, rmiConnectorServer, discoveryConfig != null?discoveryConfig.check():false, exposerName, host));
        }
    }

    public static ServiceRegistrarClient newServiceRegistrarClient(final DiscoveryConfig discoveryConfig) {

        return new ServiceRegistrarClient() {
            public <T> T create(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException {
                discoveryConfig.init();
                discoveryConfig.connect();
                String concat = "/".concat(service.getCanonicalName());
                MethodInvocationHandler h = new MethodInvocationHandler();
                RegistrationServiceHolder znode = Functions.searchRshByName.apply(discoveryConfig.retrieveAll(concat, h, exposerName)).apply(exposerName);
                LOGGER.info(PREPARE_TO_CREATE_PROXY_MESSAGE, service, znode);
                String host = znode.getHost().split(":")[0];
                Integer port = Integer.valueOf(znode.getHost().split(":")[1]);
                h.setRmiConnectorClient(rmiConnectorClient.newClient(host, port));
                h.setExposerName(exposerName);
                return (T) Proxy.newProxyInstance(ServiceRegistrarClient.class.getClassLoader(),
                        new Class<?>[]{service}, h);
            }

            @Override
            public <T> T create(Class<T> service, String exposerName, RmiConnectorClient rmiConnectorClient, String token) throws IOException, InterruptedException {
                discoveryConfig.init();
                discoveryConfig.connect();
                String concat = "/".concat(service.getCanonicalName());
                MethodInvocationHandler h = new MethodInvocationHandler();
                RegistrationServiceHolder znode = Functions.searchRshByName.apply(discoveryConfig.retrieveAll(concat, h, exposerName)).apply(exposerName);
                LOGGER.info(PREPARE_TO_CREATE_PROXY_MESSAGE, service, znode);
                String host = znode.getHost().split(":")[0];
                Integer port = Integer.valueOf(znode.getHost().split(":")[1]);
                h.setRmiConnectorClient(rmiConnectorClient.newClient(host, port));
                h.setToken(token);
                h.setExposerName(exposerName);
                return (T) Proxy.newProxyInstance(ServiceRegistrarClient.class.getClassLoader(),
                        new Class<?>[]{service}, h);
            }

            @Override
            public <T> List<T> createForAll(Class<T> service, RmiConnectorClient rmiConnectorClient) throws IOException, InterruptedException {
                String concat = "/".concat(service.getCanonicalName());
                discoveryConfig.init();
                discoveryConfig.connect();
                Set<RegistrationServiceHolder> holders = discoveryConfig.retrieveAll(concat, null);
                List<T> ts = new ArrayList<>();
                for (RegistrationServiceHolder holder : holders) {
                    LOGGER.info(PREPARE_TO_CREATE_PROXY_MESSAGE, service, holder);
                    MethodInvocationHandler h = new MethodInvocationHandler();
                    RegistrationServiceHolder znode = Functions.searchRshByName.apply(discoveryConfig.retrieveAll(concat, h, holder.getExposerName())).apply(holder.getExposerName());
                    String host = znode.getHost().split(":")[0];
                    Integer port = Integer.valueOf(znode.getHost().split(":")[1]);
                    h.setRmiConnectorClient(rmiConnectorClient.newClient(host, port));
                    ts.add((T) Proxy.newProxyInstance(ServiceRegistrarClient.class.getClassLoader(),
                            new Class<?>[]{service}, h));
                }
                return ts;
            }
        };
    }
}
