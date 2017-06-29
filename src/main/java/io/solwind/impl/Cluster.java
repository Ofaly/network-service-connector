package io.solwind.impl;

import io.solwind.api.DiscoveryConfig;
import io.solwind.api.IDiscovery;
import io.solwind.api.IExposer;
import io.solwind.api.IInjector;
import io.solwind.handler.MethodInvocationHandler;
import io.solwind.handler.RegistrationServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;

/**
 * Created by solwind on 6/14/17.
 */
public final class Cluster implements IInjector {

    public static final Logger LOGGER = LoggerFactory.getLogger(Cluster.class);

    private Cluster() {
    }

    public static IExposer exposer(String host, DiscoveryConfig discoveryConfig) throws IOException, InterruptedException {
        return new Exposer(host, discoveryConfig);
    }

    public static IExposer exposer(DiscoveryConfig discoveryConfig) throws IOException, InterruptedException {
        return new Exposer(discoveryConfig);
    }

    public IInjector createInjector() {
        return new Cluster();
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
                return (T) Proxy.newProxyInstance(IDiscovery.class.getClassLoader(),
                        new Class<?>[]{service}, new MethodInvocationHandler(znode));
            }
        };
    }
}
