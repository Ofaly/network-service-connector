package io.solwind.api;

import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by solwind on 6/14/17.
 */
public interface DiscoveryConfig {
    void init();
    void connect() throws IOException, InterruptedException;
    void push(String exposerName, RegistrationServiceHolder data) throws KeeperException, InterruptedException;
    RegistrationServiceHolder retrieve(String path);
    Properties props();
}
