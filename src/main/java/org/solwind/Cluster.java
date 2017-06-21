package org.solwind;

import java.io.IOException;
import java.lang.reflect.Proxy;

/**
 * Created by solwind on 6/14/17.
 */
public final class Cluster implements IInjector {

    private Cluster() {
    }

    public static IExposer exposer(int port, DiscoveryConfig discoveryConfig) throws IOException, InterruptedException {
        return new Exposer(port, discoveryConfig);
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
                String znode = discoveryConfig.retrieve(concat);
                return (T) Proxy.newProxyInstance(IDiscovery.class.getClassLoader(),
                        new Class<?>[]{service}, new MethodInvocationHandler(znode));
            }
        };
    }
}
