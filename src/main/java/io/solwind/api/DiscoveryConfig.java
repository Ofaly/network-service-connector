package io.solwind.api;

import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * Created by solwind on 6/14/17.
 */
public interface DiscoveryConfig {
    void init();
    void connect() throws IOException, InterruptedException;
    void push(Class className, RegistrationServiceHolder data) throws KeeperException, InterruptedException;
    Set<RegistrationServiceHolder> retrieveAll(String path);
    Properties props();
}
