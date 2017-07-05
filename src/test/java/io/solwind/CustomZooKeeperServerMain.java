package io.solwind;

import org.apache.zookeeper.server.ZooKeeperServerMain;

/**
 * Created by theso on 7/5/2017.
 */
public class CustomZooKeeperServerMain extends ZooKeeperServerMain {
    void stop() {
        this.shutdown();
    }
}
