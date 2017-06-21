package org.solwind;

import org.apache.zookeeper.KeeperException;

/**
 * Created by solwind on 6/14/17.
 */
public interface IExposer {
    <T> void expose(T testServiceClass) throws KeeperException, InterruptedException;
    void stop() throws InterruptedException;
}
