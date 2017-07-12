package io.solwind.api;

import io.solwind.handler.RegistrationServiceHolder;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by solwind on 6/14/17.
 */
public interface DiscoveryConfig {
    void init();
    void connect() throws IOException, InterruptedException;
    void push(Class className, RegistrationServiceHolder data) throws KeeperException, InterruptedException;
    Set<RegistrationServiceHolder> retrieveAll(String path, Consumer<Set<RegistrationServiceHolder>> consumer, String exposerNameIfNewNeeded);
    Properties props();
}
