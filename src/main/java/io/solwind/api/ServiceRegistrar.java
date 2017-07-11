package io.solwind.api;

import org.apache.zookeeper.KeeperException;

/**
 * Created by solwind on 6/14/17.
 */
public interface ServiceRegistrar {
    <T> void register(T testServiceClass, String version, String shortDescription) throws KeeperException, InterruptedException;
    <T> void register(T testServiceClass, String version, String shortDescription, TokenSecurityHandler tokenSecurityHandler) throws KeeperException, InterruptedException;
    void stop() throws InterruptedException;
}
