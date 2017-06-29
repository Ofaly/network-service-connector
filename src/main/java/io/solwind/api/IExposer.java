package io.solwind.api;

import org.apache.zookeeper.KeeperException;

/**
 * Created by solwind on 6/14/17.
 */
public interface IExposer {
    <T> void expose(T testServiceClass, String version, String shortDescription) throws KeeperException, InterruptedException;
    void stop() throws InterruptedException;
}
